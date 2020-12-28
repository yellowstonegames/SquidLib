package squidpony.squidmath;

/**
 * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, or 6 dimensions and
 * a long for state; like {@link PointHash} but faster and maybe not as high-quality. This implementation has
 * high enough quality to be useful as a source of random numbers based on positions, but would likely not be a good
 * option in a hash table (or at least not as good as the tailored implementation of {@link Coord#hashCode()}, for
 * instance). At low dimensions, this is a little faster than {@link PointHash}, but this class doesn't slow 
 * down much at all as more dimensions are used, while PointHash and most other implementations do slow down. You
 * can also consider {@link IntPointHash} if your input and output types are usually int, since it's even faster.
 * <br>
 * This implements {@link IPointHash} and has a long it uses internally for state, exposed by {@link #getState()}.
 */
public final class HastyPointHash extends IPointHash.LongImpl {

    public static final HastyPointHash INSTANCE = new HastyPointHash();

    @Override
    public int hashWithState(int x, int y, int state) {
        return (int)hashAll(x, y, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int state) {
        return (int)hashAll(x, y, z, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int state) {
        return (int)hashAll(x, y, z, w, state);
    }

    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int state) {
        return (int)hashAll(x, y, z, w, u, state);
    }
    
    @Override
    public int hashWithState(int x, int y, int z, int w, int u, int v, int state) {
        return (int)hashAll(x, y, z, w, u, v, state);
    }
    
    public long getState(){
        return state;
    }

    /**
     * Gets a 64-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y point with the given state
     */
    public static long hashAll(long x, long y, long s) {
        y += s * 0xD1B54A32D192ED03L;
        x += y * 0xABC98388FB8FAC03L;
        s += x * 0x8CB92BA72F3D8DD7L;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }

    /**
     * Gets a 64-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 64-bit hash of the x,y,z point with the given state
     */
    public static long hashAll(long x, long y, long z, long s) {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }

    /**
     * Gets a 64-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long s) {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }

    /**
     * Gets a 64-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long. This
     * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w,u point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long s) {
        u += s * 0xE60E2B722B53AEEBL;
        w += u * 0xCEBD76D9EDB6A8EFL;
        z += w * 0xB9C9AA3A51D00B65L;
        y += z * 0xA6F5777F6F88983FL;
        x += y * 0x9609C71EB7D03F7BL;
        s += x * 0x86D516E50B04AB1BL;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }

    /**
     * Gets a 64-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
     * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 64-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long v, long s) {
        v += s * 0xE95E1DD17D35800DL;
        u += v * 0xD4BC74E13F3C782FL;
        w += u * 0xC1EDBC5B5C68AC25L;
        z += w * 0xB0C8AC50F0EDEF5DL;
        y += z * 0xA127A31C56D1CDB5L;
        x += y * 0x92E852C80D153DB3L;
        s += x * 0x85EB75C3024385C3L;
        return ((s = (s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) ^ s >>> 25);
    }
    /**
     * Gets an 8-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y point with the given state
     */
    public static int hash256(long x, long y, long s) {
        y += s * 0xD1B54A32D192ED03L;
        x += y * 0xABC98388FB8FAC03L;
        s += x * 0x8CB92BA72F3D8DD7L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 8-bit hash of the x,y,z point with the given state
     */
    public static int hash256(long x, long y, long z, long s) {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long s) {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
    }


    /**
     * Gets an 8-bit point hash of a 5D point (x, y, z, w, and u are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w,u point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long s) {
        u += s * 0xE60E2B722B53AEEBL;
        w += u * 0xCEBD76D9EDB6A8EFL;
        z += w * 0xB9C9AA3A51D00B65L;
        y += z * 0xA6F5777F6F88983FL;
        x += y * 0x9609C71EB7D03F7BL;
        s += x * 0x86D516E50B04AB1BL;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
    }

    /**
     * Gets an 8-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
     * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long v, long s) {
        v += s * 0xE95E1DD17D35800DL;
        u += v * 0xD4BC74E13F3C782FL;
        w += u * 0xC1EDBC5B5C68AC25L;
        z += w * 0xB0C8AC50F0EDEF5DL;
        y += z * 0xA127A31C56D1CDB5L;
        x += y * 0x92E852C80D153DB3L;
        s += x * 0x85EB75C3024385C3L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 56);
    }
    /**
     * Gets a 5-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y point with the given state
     */
    public static int hash32(long x, long y, long s) {
        y += s * 0xD1B54A32D192ED03L;
        x += y * 0xABC98388FB8FAC03L;
        s += x * 0x8CB92BA72F3D8DD7L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 5-bit hash of the x,y,z point with the given state
     */
    public static int hash32(long x, long y, long z, long s) {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long s) {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
    }

    /**
     * Gets a 5-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
     * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long u, long v, long s) {
        v += s * 0xE95E1DD17D35800DL;
        u += v * 0xD4BC74E13F3C782FL;
        w += u * 0xC1EDBC5B5C68AC25L;
        z += w * 0xB0C8AC50F0EDEF5DL;
        y += z * 0xA127A31C56D1CDB5L;
        x += y * 0x92E852C80D153DB3L;
        s += x * 0x85EB75C3024385C3L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 59);
    }
    
    /**
     * Gets a 6-bit point hash of a 2D point (x and y are both longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y point with the given state
     */
    public static int hash64(long x, long y, long s) {
        y += s * 0xD1B54A32D192ED03L;
        x += y * 0xABC98388FB8FAC03L;
        s += x * 0x8CB92BA72F3D8DD7L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 3D point (x, y, and z are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param s the state/seed; any long
     * @return 6-bit hash of the x,y,z point with the given state
     */
    public static int hash64(long x, long y, long z, long s) {
        z += s * 0xDB4F0B9175AE2165L;
        y += z * 0xBBE0563303A4615FL;
        x += y * 0xA0F2EC75A1FE1575L;
        s += x * 0x89E182857D9ED689L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 4D point (x, y, z, and w are all longs) and a state/seed as a long. This point
     * hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long s) {
        w += s * 0xE19B01AA9D42C633L;
        z += w * 0xC6D1D6C8ED0C9631L;
        y += z * 0xAF36D01EF7518DBBL;
        x += y * 0x9A69443F36F710E7L;
        s += x * 0x881403B9339BD42DL;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
    }

    /**
     * Gets a 6-bit point hash of a 6D point (x, y, z, w, u, and v are all longs) and a state/seed as a long. This
     * point hash has close to the best speed of any algorithms tested, and though its quality is mediocre for
     * traditional uses of hashing (such as hash tables), it's sufficiently random to act as a positional RNG.
     * <br>
     * This uses a technique related to the one used by Martin Roberts for his golden-ratio-based sub-random
     * sequences, where each axis is multiplied by a different constant, and the choice of constants depends on the
     * number of axes but is always related to a generalized form of golden ratios, repeatedly dividing 1.0 by the
     * generalized ratio. See
     * <a href="http://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">Roberts' article</a>
     * for some more information on how he uses this, but we do things differently because we want random-seeming
     * results instead of separated sub-random results.
     * @param x x position; any long
     * @param y y position; any long
     * @param z z position; any long
     * @param w w position; any long
     * @param u u position; any long
     * @param v v position; any long
     * @param s the state; any long
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long u, long v, long s) {
        v += s * 0xE95E1DD17D35800DL;
        u += v * 0xD4BC74E13F3C782FL;
        w += u * 0xC1EDBC5B5C68AC25L;
        z += w * 0xB0C8AC50F0EDEF5DL;
        y += z * 0xA127A31C56D1CDB5L;
        x += y * 0x92E852C80D153DB3L;
        s += x * 0x85EB75C3024385C3L;
        return (int)(((s ^ s >>> 27 ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L) >>> 58);
    }

    public static int hash256_alt(final long x, final long y, final long z, final long w, final long seed) {
        long a = 0x632BE59BD9B4E019L;
        return (int) ((0x9E3779B97F4A7C15L
                + (a ^= 0x85157AF5L * seed + x)
                + (a ^= 0x85157AF5L * x + y)
                + (a ^= 0x85157AF5L * y + z)
                + (a ^= 0x85157AF5L * z + w)
                + (a ^= 0x85157AF5L * w + seed)) * a >>> 56);
    }

}
