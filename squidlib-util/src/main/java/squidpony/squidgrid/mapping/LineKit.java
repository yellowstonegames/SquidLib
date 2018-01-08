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
     * A constant that represents the encoded pattern for a 4x4 square with all lines possible except those that
     * would extend to touch cells adjacent to the 4x4 area. Meant to restrict cells within the square area by using
     * bitwise AND with an existing encoded pattern as another long, as with {@code LineKit.interiorSquare & encoded}.
     * If you limit the area to the square with this, you may sometimes want to add a border, and for that you can use
     * {@link #exteriorSquare} and bitwise OR that with the restricted area.
     */
    public final static long interiorSquare = 0x3776BFFEBFFE9DDCL,
    /**
     * A constant that represents the encoded pattern for a 4x4 square with only the lines along the border. Meant to
     * either restrict cells to the border by using bitwise AND with an existing encoded pattern as another long, as
     * with {@code LineKit.exteriorSquare & encoded}, or to add a border to an existing pattern with bitwise OR, as with
     * {@code LineKit.exteriorSquare | encoded}.
     */
    exteriorSquare = 0x3556A00AA00A955CL,
    /**
     * A constant that represents the encoded pattern for a 4x4 plus pattern with only the lines along the border. This
     * pattern has no lines in the corners of the 4x4 area, but has some lines in all other cells, though none that
     * would touch cells adjacent to this 4x4 area. Meant to restrict cells to the border by using bitwise AND with an
     * existing encoded pattern as another long, as with {@code LineKit.interiorPlus & encoded}.
     */
    interiorPlus = 0x03603FF69FFC09C0L,
    /**
     * A constant that represents the encoded pattern for a 4x4 plus pattern with only the lines along the border. This
     * pattern has no lines in the corners of the 4x4 area, but has some lines in all other cells, though none that
     * would touch cells adjacent to this 4x4 area. Meant to either restrict cells to the border by using bitwise AND
     * with an existing encoded pattern as another long, as with {@code LineKit.exteriorPlus & encoded}, or to add a
     * border to an existing pattern with bitwise OR, as with {@code LineKit.exteriorPlus | encoded}.
     */
    exteriorPlus = 0x03603C96963C09C0L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 square. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. This is expected to be flipped
     * using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make the other corners. If middle
     * pieces are wanted that touch everything but the upper border, you can use
     * {@code (LineKit.interiorSquareLarge | LineKit.flipHorizontal4x4(LineKit.interiorSquareLarge))}. If you want it to
     * touch everything but the left border, you can use
     * {@code (LineKit.interiorSquareLarge | LineKit.flipVertical4x4(LineKit.interiorSquareLarge))}.
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorSquareLarge = 0xFFFEFFFEFFFEDDDCL,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 square border. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorSquareLarge = 0x000A000A000A555CL,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 6x6 square centered in an 8x8
     * space. A 3x3 square will be filled of the 4x4 area this represents. No lines will touch the upper or left
     * borders, but they do extend into the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     */
    shallowInteriorSquareLarge = 0xFFE0FFE0DDC00000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 6x6 square border centered in an
     * 8x8 space. This consists of a 3-cell-long vertical line and a 3-cell-long horizontal line. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    shallowExteriorSquareLarge = 0x00A000A055C00000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 4x4 square centered in an 8x8
     * space. A 2x2 square will be filled of the 4x4 area this represents. No lines will touch the upper or left
     * borders, but they do extend into the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     */
    shallowerInteriorSquareLarge = 0xFE00DC0000000000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 4x4 square border centered in an
     * 8x8 space. This consists of a 2-cell-long vertical line and a 2-cell-long horizontal line. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    shallowerExteriorSquareLarge = 0x0A005C0000000000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 plus shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves the
     * upper left 2x2 area blank, and touches all of the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorPlusLarge = 0xFFFEFFDCFE00DC00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 plus shape border. No lines
     * will touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves
     * the upper left 2x2 area blank, as well as all but one each of the bottom and right border cells. This is expected
     * to be flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorPlusLarge = 0x000A035C0A005C00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 circle shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This is expected to be
     * flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorCircleLarge = 0xFFFEFFFCFFC0DC00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 circular border. No lines
     * will touch the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one curving line. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make other corners.
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorCircleLarge = 0x000A003C03C05C00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 diamond shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves the
     * upper left 2x2 area blank, and touches all of the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners. This has more of a
     * fine angle than {@link #interiorPlusLarge}, which is otherwise similar.
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorDiamondLarge = 0xFFFCFFC0FC00C000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 diamond shape border. No
     * lines will touch the upper or left borders, but they do extend into the lower and right borders. This pattern
     * leaves the upper left 2x2 area blank, as well as all but one each of the bottom and right border cells. This is
     * expected to be flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other
     * corners. This has more of a fine angle than {@link #exteriorPlusLarge}, which is otherwise similar.
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorDiamondLarge = 0x003C03C03C00C000L;

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
    /**
     * Fills a 4x4 area of the given 2D char array {@code into} by interpreting the bits of the given long as line
     * information. Uses the given char array {@code symbols}, which must have at least 16 elements and is usually one
     * of {@link #light}, {@link #heavy}, {@link #lightAlt}, or {@link #heavyAlt}, with the last two usable only if
     * using a font that supports the chars {@code ╴╵╶╷} (this is true for Iosevka and Source Code Pro, for instance,
     * but not Inconsolata or GoMono).
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @param symbols a 16-element-or-larger char array; usually a constant in this class like {@link #light}
     * @param into a 2D char array that will be modified in a 4x4 area
     * @param startX the first x position to modify in into
     * @param startY the first y position to modify in into
     * @return into, after modification
     */
    public static char[][] decodeInto4x4(long encoded, char[] symbols, char[][] into, int startX, int startY)
    {
        for (int i = 0; i < 16; i++) {
            into[(i & 3) + startX][(i >> 2) + startY] = symbols[(int) (encoded >>> (i << 2) & 15L)];
        }
        return into;
    }

    /**
     * Reads a 2D char array {@code decoded}, which must be at least 4x4 in size, and returns a long that encodes the cells from 0,0 to
     * 3,3 in a way that this class can interpret and manipulate. The 2D array {@code decoded} must contain box drawing
     * symbols, which can be any of those from {@link #light}, {@link #heavy}, {@link #lightAlt}, or {@link #heavyAlt}.
     * Valid chars are {@code ╴╵┘╶─└┴╷┐│┤┌┬├┼╸╹┛╺━┗┻╻┓┃┫┏┳┣╋}; any other chars will be treated as empty space.
     * @param decoded a 2D char array that must be at least 4x4 and should usually contain box drawing characters
     * @return a long that encodes the box drawing information in decoded so this class can manipulate it
     */
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
                    v |= 5L << (i << 2);
                    break;
                case '│':
                case '┃':
                    v |= 10L << (i << 2);
                    break;
                case '┘':
                case '┛':
                    v |= 3L << (i << 2);
                    break;
                case '└':
                case '┗':
                    v |= 6L << (i << 2);
                    break;
                case '┐':
                case '┓':
                    v |= 9L << (i << 2);
                    break;
                case '┌':
                case '┏':
                    v |= 12L << (i << 2);
                    break;
                case '┴':
                case '┻':
                    v |= 7L << (i << 2);
                    break;
                case '┤':
                case '┫':
                    v |= 11L << (i << 2);
                    break;
                case '┬':
                case '┳':
                    v |= 13L << (i << 2);
                    break;
                case '├':
                case '┣':
                    v |= 14L << (i << 2);
                    break;
                case '┼':
                case '╋':
                    v |= 15L << (i << 2);
                    break;
                case '╴':
                case '╸':
                    v |= 1L << (i << 2);
                    break;
                case '╵':
                case '╹':
                    v |= 2L << (i << 2);
                    break;
                case '╶':
                case '╺':
                    v |= 4L << (i << 2);
                    break;
                case '╷':
                case '╻':
                    v |= 8L << (i << 2);
                    break;
            }
        }
        return v;
    }

    /**
     * Makes a variant on the given encoded 4x4 pattern so the left side is flipped to the right side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument flipped left-to-right
     */
    public static long flipHorizontal4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 10L) | ((encoded >>> i4 & 1) << 2L) | ((encoded >>> i4 & 4L) >>> 2)) << (i + 3 - ((i & 3) << 1) << 2);
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the top side is flipped to the bottom side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument flipped top-to-bottom
     */
    public static long flipVertical4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 5L) | ((encoded >>> i4 & 2L) << 2) | ((encoded >>> i4 & 8L) >>> 2)) << (i + 12 - ((i >> 2) << 3) << 2);
        }
        return v;
    }

    /**
     * Makes a variant on the given encoded 4x4 pattern so the x and y axes are interchanged, making the top side become
     * the left side and vice versa, while the bottom side becomes the right side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument transposed top-to-left and bottom-to-right
     */
    public static long transpose4x4(long encoded)
    {
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= (((encoded >>> i4 & 5L) << 1) | ((encoded >>> i4 & 10L) >>> 1)) << ((i4 >>> 2 & 12L) | ((i4 & 12L) << 2));
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the lines are rotated 90 degrees clockwise, changing their
     * positions as well as what chars they will decode to. This can be called twice to get a 180 degree rotation, but
     * {@link #rotateCounterclockwise(long)} should be used for a 270 degree rotation.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument rotated 90 degrees clockwise
     */
    public static long rotateClockwise(long encoded)
    {
        // this is functionally equivalent to, but faster than, the following:
        // return flipHorizontal4x4(transpose4x4(encoded));
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= (((encoded >>> i4 & 7L) << 1) | ((encoded >>> i4 & 8L) >>> 3)) << ((~i4 >>> 2 & 12L) | ((i4 & 12L) << 2));
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the lines are rotated 90 degrees counterclockwise, changing
     * their positions as well as what chars they will decode to. This can be called twice to get a 180 degree rotation,
     * but {@link #rotateClockwise(long)} should be used for a 270 degree rotation.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument rotated 90 degrees counterclockwise
     */
    public static long rotateCounterclockwise(long encoded)
    {
        // this is functionally equivalent to, but faster than, the following:
        // return flipVertical4x4(transpose4x4(encoded));
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= ((encoded >>> (i4 + 1) & 7L) | ((encoded >>> i4 & 1L) << 3)) << ((i4 >>> 2 & 12L) | ((~i4 & 12L) << 2));
        }
        return v;
    }

//    public static void main(String[] args)
//    {
//        System.out.printf("interiorSquare = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {'┌', '┬', '┬', '┐'},
//                        {'├', '┼', '┼', '┤'},
//                        {'├', '┼', '┼', '┤'},
//                        {'└', '┴', '┴', '┘'},
//                })));
//        System.out.printf("exteriorSquare = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {'┌', '─', '─', '┐'},
//                        {'│', ' ', ' ', '│'},
//                        {'│', ' ', ' ', '│'},
//                        {'└', '─', '─', '┘'},
//                })));
//        System.out.printf("interiorPlus = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', '┌', '┐', ' '},
//                        {'┌', '┼', '┼', '┐'},
//                        {'└', '┼', '┼', '┘'},
//                        {' ', '└', '┘', ' '},
//                })));
//        System.out.printf("exteriorPlus = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', '┌', '┐', ' '},
//                        {'┌', '┘', '└', '┐'},
//                        {'└', '┐', '┌', '┘'},
//                        {' ', '└', '┘', ' '},
//                })));
//        System.out.printf("interiorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {'┌', '┬', '┬', '┬'},
//                        {'├', '┼', '┼', '┼'},
//                        {'├', '┼', '┼', '┼'},
//                        {'├', '┼', '┼', '┼'},
//                })));
//        System.out.printf("exteriorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {'┌', '─', '─', '─'},
//                        {'│', ' ', ' ', ' '},
//                        {'│', ' ', ' ', ' '},
//                        {'│', ' ', ' ', ' '},
//                })));
//        System.out.printf("interiorPlusLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', '┌', '┬'},
//                        {' ', ' ', '├', '┼'},
//                        {'┌', '┬', '┼', '┼'},
//                        {'├', '┼', '┼', '┼'},
//                })));
//        System.out.printf("exteriorPlusLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', '┌', '─'},
//                        {' ', ' ', '│', ' '},
//                        {'┌', '─', '┘', ' '},
//                        {'│', ' ', ' ', ' '},
//                })));
//        System.out.printf("interiorCircleLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', '┌', '┬'},
//                        {' ', '┌', '┼', '┼'},
//                        {'┌', '┼', '┼', '┼'},
//                        {'├', '┼', '┼', '┼'},
//                })));
//        System.out.printf("exteriorCircleLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', '┌', '─'},
//                        {' ', '┌', '┘', ' '},
//                        {'┌', '┘', ' ', ' '},
//                        {'│', ' ', ' ', ' '},
//                })));
//        System.out.printf("interiorDiamondLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', '┌'},
//                        {' ', ' ', '┌', '┼'},
//                        {' ', '┌', '┼', '┼'},
//                        {'┌', '┼', '┼', '┼'},
//                })));
//        System.out.printf("exteriorDiamondLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', '┌'},
//                        {' ', ' ', '┌', '┘'},
//                        {' ', '┌', '┘', ' '},
//                        {'┌', '┘', ' ', ' '},
//                })));
//        System.out.printf("shallowInteriorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', ' '},
//                        {' ', '┌', '┬', '┬'},
//                        {' ', '├', '┼', '┼'},
//                        {' ', '├', '┼', '┼'},
//                })));
//        System.out.printf("shallowExteriorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', ' '},
//                        {' ', '┌', '─', '─'},
//                        {' ', '│', ' ', ' '},
//                        {' ', '│', ' ', ' '},
//                })));
//        System.out.printf("shallowerInteriorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', ' '},
//                        {' ', ' ', ' ', ' '},
//                        {' ', ' ', '┌', '┬'},
//                        {' ', ' ', '├', '┼'},
//                })));
//        System.out.printf("shallowerExteriorSquareLarge = 0x%016XL\n", LineKit.encode4x4(DungeonUtility.transposeLines(new char[][]
//                {
//                        {' ', ' ', ' ', ' '},
//                        {' ', ' ', ' ', ' '},
//                        {' ', ' ', '┌', '─'},
//                        {' ', ' ', '│', ' '},
//                })));
//    }
}
