package squidpony.squidmath;

/**
 * A group of similar methods for getting hashes of points based on long coordinates in 2, 3, 4, or 6 dimensions and
 * a long for state. Internally, the methods here are based on a simplified version of Hive from {@link CrossHash};
 * this version does not include the somewhat-involved finalization step. Omitting finalization helps this class be
 * somewhat faster, but also makes it so it doesn't avalanche very well. Avalanche refers to the property of a hash
 * where one changed bit in the input(s) or state has a chance to change any of the output's bits, and should
 * usually change about 50% of them. You can call {@link #avalanche(long)} on the result of a hashAll() call to put
 * the finalization step back. This has rather good quality when {@link #avalanche(long)} is used, but most usage
 * will not be able to discern a difference between the quality of this (with or without avalanche) and the quality of
 * {@link HastyPointHash}, where HastyPointHash is a fair amount faster. If your state and input are ints anyway,
 * consider {@link IntPointHash}, since it's faster than the long-based hashes and still has perfectly fine quality.
 * <br>
 * This implements {@link IPointHash} and has a long it uses internally for state, exposed by {@link #getState()}.
 */
public final class PointHash extends IPointHash.LongImpl
{
    public static final PointHash INSTANCE = new PointHash();

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
     * If it's important for some usage that one bit of change in any parameters cause roughly 50% of all bits in
     * the result to change, then you can call this on the output of any hashAll() overload to improve the bit
     * avalanche qualities.
     * @param state the output of hashAll() in this class
     * @return a significantly-mixed-around long obtained from state
     */
    public static long avalanche(long state)
    {
        state = (state ^ state >>> 33) * 0xFF51AFD7ED558CCDL;
        return (state ^ state >>> 33) * 0xC4CEB9FE1A85EC53L;
    }
    /**
     *
     * @param x
     * @param y
     * @param state
     * @return 64-bit hash of the x,y point with the given state
     */
    public static long hashAll(long x, long y, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
        return state ^ state >>> 31;
        
//            return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
//                    ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
//                            state * 0x9E3779B97F4A7C15L 
//                                    | 1L)) ^ y >>> 24) 
//                            | 1L)) ^ x >>> 24);

//            state = (state ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL | 1L;
//            x *= state;
//            y *= state;
//            return state ^ (x << 26 | x >>> 38) + (y << 23 | y >>> 41);

//        return ((x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26 ^ 0x9183A1F4F348E683L) * (
//                ((y = ((y *= 0x6C8E9CF570932BD5L) ^ y >>> 26 ^ 0x9183A1F4F348E683L) * (
//                                        state * 0x9E3779B97F4A7C15L
//                                | 1L)) ^ y >>> 24)
//                        | 1L)) ^ x >>> 24);

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24;
//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31);
        //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L)) ^ (state >>> 22)));
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param state
     * @return 64-bit hash of the x,y,z point with the given state
     */
    public static long hashAll(long x, long y, long z, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
        return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

        //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param state
     * @return 64-bit hash of the x,y,z,w point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
        return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24
//                    ^ (w = ((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state) ^ w >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

        //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
    }

    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param u
     * @param state
     * @return 64-bit hash of the x,y,z,w,u point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
        return state ^ state >>> 31;
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param u
     * @param v
     * @param state
     * @return 64-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static long hashAll(long x, long y, long z, long w, long u, long v, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state -= ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL;
        return state ^ state >>> 31;

//            return (x = ((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L)) ^ x >>> 24
//                    ^ (y = ((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state) ^ y >>> 24
//                    ^ (z = ((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state) ^ z >>> 24
//                    ^ (w = ((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state) ^ w >>> 24
//                    ^ (u = ((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state) ^ u >>> 24
//                    ^ (v = ((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state) ^ v >>> 24;

//            return ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ^ ((state = ((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL) ^ state >>> 31)
//                    ;

        //state *= 0x352E9CF570932BDDL;
//            return (((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ u) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L)) ^ (state >>> 22)));
    }
    /**
     *
     * @param x
     * @param y
     * @param state
     * @return 8-bit hash of the x,y point with the given state
     */         
    public static int hash256(long x, long y, long state) {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)) >>> 56);

//                return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                    ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((y ^ state) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((x ^ state) | 0xA529L))) >>> 56);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param state
     * @return 8-bit hash of the x,y,z point with the given state
     */
    public static int hash256(long x, long y, long z, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param state
     * @return 8-bit hash of the x,y,z,w point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param u
     * @param v
     * @param state
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash256(long x, long y, long z, long w, long u, long v, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 56);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 56);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 56);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 56);
    }
    /**
     *
     * @param x
     * @param y
     * @param state
     * @return 5-bit hash of the x,y point with the given state
     */
    public static int hash32(long x, long y, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);
        
//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param state
     * @return 5-bit hash of the x,y,z point with the given state
     */
    public static int hash32(long x, long y, long z, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y - z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z - x) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x - y) | 0xA529L))) >>> 59);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param state
     * @return 5-bit hash of the x,y,z,w point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param u
     * @param v
     * @param state
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash32(long x, long y, long z, long w, long u, long v, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 59);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 59);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 59);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 59);
    }

    /**
     *
     * @param x
     * @param y
     * @param state
     * @return 6-bit hash of the x,y point with the given state
     */
    public static int hash64(long x, long y, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);


        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param state
     * @return 6-bit hash of the x,y,z point with the given state
     */
    public static int hash64(long x, long y, long z, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y - z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z - x) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ x - y) | 0xA529L))) >>> 58);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param state
     * @return 6-bit hash of the x,y,z,w point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ w) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
    }
    /**
     *
     * @param x
     * @param y
     * @param z
     * @param w
     * @param u
     * @param v
     * @param state
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state
     */
    public static int hash64(long x, long y, long z, long w, long u, long v, long state)
    {
        state *= 0x9E3779B97F4A7C15L;
        long other = 0x60642E2A34326F15L;
        state ^= (other += (x ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (y ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (z ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (w ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (u ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        state = (state << 54 | state >>> 10);
        state ^= (other += (v ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
        return (int)(state - ((state << 54 | state >>> 10) + (other ^ other >>> 29)) * 0x94D049BB133111EBL >>> 58);

//            return (int) (((((x *= 0x6C8E9CF570932BD5L) ^ x >>> 26) * (state = state * 0x9E3779B97F4A7C15L | 1L))
//                    ^ (((y *= 0x5851F42D4C957F2DL) ^ y >>> 26) * state)
//                    ^ (((z *= 0xAEF17502108EF2D9L) ^ z >>> 26) * state)
//                    ^ (((w *= 0x94D049BB133111EBL) ^ w >>> 26) * state)
//                    ^ (((u *= 0xBF58476D1CE4E5B9L) ^ u >>> 26) * state)
//                    ^ (((v *= 0xC6BC279692B5CC85L) ^ v >>> 26) * state)
//            ) >>> 58);

//            return (int) ((
//                    ((state = ((state = x ^ ((state += 0x9E3779B97F4A7C15L ^ y) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = y ^ ((state += 0x9E3779B97F4A7C15L ^ z) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = z ^ ((state += 0x9E3779B97F4A7C15L ^ w) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = w ^ ((state += 0x9E3779B97F4A7C15L ^ u) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((state = ((state = u ^ ((state += 0x9E3779B97F4A7C15L ^ v) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//                            ^ ((((state = v ^ ((state += 0x9E3779B97F4A7C15L ^ x) ^ state >>> 30) * 0xBF58476D1CE4E5B9L) ^ state >>> 27) * 0x94D049BB133111EBL))
//            ) >>> 58);

        //state *= 0x352E9CF570932BDDL;
//            return (int) ((((state = ((state += 0x6C8E9CD570932BD5L ^ x) ^ (state >>> 25)) * ((state ^ y) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ y) ^ (state >>> 25)) * ((state ^ z) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ z) ^ (state >>> 25)) * ((state ^ w) | 0xA529L)) ^ (state >>> 22)) ^
//                    ((state = ((state += 0x6C8E9CD570932BD5L ^ u) ^ (state >>> 25)) * ((state ^ v) | 0xA529L)) ^ (state >>> 22)) ^
//                    (((state += 0x6C8E9CD570932BD5L ^ v) ^ (state >>> 25)) * ((state ^ x) | 0xA529L))) >>> 58);
    }

}
