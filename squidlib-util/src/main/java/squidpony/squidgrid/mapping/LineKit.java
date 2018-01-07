package squidpony.squidgrid.mapping;

/**
 * Tools for constructing patterns using box-drawing characters.
 * <br>
 * Created by Tommy Ettinger on 1/6/2018.
 */
public class LineKit {
    public static final char[] lightAlt = " ╴╵┘╶─└┴╷┐│┤┌┬├┼".toCharArray(),
                               heavyAlt = " ╸╹┛╺━┗┻╻┓┃┫┏┳┣╋".toCharArray(),
                               light    = " ─│┘──└┴│┐│┤┌┬├┼".toCharArray(),
                               heavy    = " ━┃┛━━┗┻┃┓┃┫┏┳┣╋".toCharArray();
    //                                     0123456789ABCDEF

    /**
     * Produces a 4x4 2D char array by interpreting the bits of the given long as line information. Uses the box drawing
     * chars from {@link #light}, which are compatible with most fonts.
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @return a 4x4 2D char array containing elements from symbols assigned based on encoded
     */
    public static char[][] decode4x4(long encoded)
    {
        return decode4x4(encoded, light);
    }
    /**
     * Produces a 4x4 2D char array by interpreting the bits of the given long as line information. Uses the given char
     * array, which must have at least 16 elements and is usually one of {@link #light}, {@link #heavy},
     * {@link #lightAlt}, or {@link #heavyAlt}, with the last two usable only if using a font that supports the chars
     * {@code ╴╵╶╷} (this is true for Iosevka and Source Code Pro, for instance, but not Inconsolata or GoMono).
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @param symbols a 16-element-or-larger char array; usually a constant in this class like {@link #light}
     * @return a 4x4 2D char array containing elements from symbols assigned based on encoded
     */
    public static char[][] decode4x4(long encoded, char[] symbols)
    {
        char[][] v = new char[4][4];
        for (int i = 0; i < 16; i++) {
            v[i & 3][i >> 2] = symbols[(int) (encoded >>> (i << 2) & 15L)];
        }
        return v;
    }
    public static char[][] decodeInto4x4(long encoded, char[] symbols, char[][] into, int startX, int startY)
    {
        for (int i = 0; i < 16; i++) {
            into[(i & 3) + startX][(i >> 2) + startY] = symbols[(int) (encoded >>> (i << 2) & 15L)];
        }
        return into;
    }
    public static long encode4x4(char[][] decoded)
    {
        long v = 0L;
        for (int i = 0; i < 16; i++) {
            switch (decoded[i & 3][i >> 2])
            {
                // ╴╵┘╶─└┴╷┐│┤┌┬├┼
                // ╸╹┛╺━┗┻╻┓┃┫┏┳┣╋
                //0123456789ABCDEF
                case '─':
                case '━':
                    v |= 5 << (i << 2);
                    break;
                case '│':
                case '┃':
                    v |= 10 << (i << 2);
                    break;
                case '┘':
                case '┛':
                    v |= 3 << (i << 2);
                    break;
                case '└':
                case '┗':
                    v |= 6 << (i << 2);
                    break;
                case '┐':
                case '┓':
                    v |= 9 << (i << 2);
                    break;
                case '┌':
                case '┏':
                    v |= 12 << (i << 2);
                    break;
                case '┴':
                case '┻':
                    v |= 7 << (i << 2);
                    break;
                case '┤':
                case '┫':
                    v |= 11 << (i << 2);
                    break;
                case '┬':
                case '┳':
                    v |= 13 << (i << 2);
                    break;
                case '├':
                case '┣':
                    v |= 14 << (i << 2);
                    break;
                case '┼':
                case '╋':
                    v |= 15 << (i << 2);
                    break;
                case '╴':
                case '╸':
                    v |= 1 << (i << 2);
                    break;
                case '╵':
                case '╹':
                    v |= 2 << (i << 2);
                    break;
                case '╶':
                case '╺':
                    v |= 4 << (i << 2);
                    break;
                case '╷':
                case '╻':
                    v |= 8 << (i << 2);
                    break;
            }
        }
        return v;
    }
    public static long flipHorizontal4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 10L) | ((encoded >>> i4 & 1) << 2) | ((encoded >>> i4 & 4) >>> 2)) << (i + 3 - ((i & 3) << 1) << 2);
        }
        return v;
    }
    public static long flipVertical4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 5L) | ((encoded >>> i4 & 2) << 2) | ((encoded >>> i4 & 8) >>> 2)) << (i + 12 - ((i >> 2) << 3) << 2);
        }
        return v;
    }
}
