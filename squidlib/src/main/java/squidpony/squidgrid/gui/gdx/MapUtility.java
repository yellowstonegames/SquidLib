package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import squidpony.ArrayTools;
import squidpony.squidmath.PerlinNoise;

/**
 * Created by Tommy Ettinger on 7/9/2017.
 */
public class MapUtility {
    /**
     * Produces a Color[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.
     *
     * @param map a char[][] containing foreground characters
     * @return a 2D array of Colors with the same size as map, that can be used for the corresponding chars
     */
    public static Color[][] generateDefaultColors(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        Color[][] portion = new Color[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = SColor.LIMITED_PALETTE[2];
                        break;
                    case '.':
                    case ':':
                        portion[i][j] = SColor.LIMITED_PALETTE[3];
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = SColor.LIMITED_PALETTE[4];
                        break;
                    case ',':
                    case '~':
                        portion[i][j] = SColor.LIMITED_PALETTE[5];
                        break;
                    case '"':
                        portion[i][j] = SColor.LIMITED_PALETTE[20];
                        break;
                    case '^':
                        portion[i][j] = SColor.LIMITED_PALETTE[6];
                        break;
                    default:
                        portion[i][j] = SColor.LIMITED_PALETTE[1];
                }
            }
        }
        return portion;
    }

    /**
     * Produces a Color[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * This overload also takes a char that corresponds to deep non-water lakes (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * can produce) and a Color to use for that deep liquid, as well as a char for shallow lakes and a Color for that
     * shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.  Deep and shallow lakes of non-water will use the given
     * Color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you don't need
     * this overload, and {@link #generateDefaultColors(char[][])} will be fine.
     *
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the Color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the Color to use for shallow parts of non-water lakes
     * @return a 2D array of Colors with the same size as map, that can be used for the corresponding chars
     */
    public static Color[][] generateDefaultColors(char[][] map, char deepChar, Color deepColor,
                                                 char shallowChar, Color shallowColor) {

        int width = map.length;
        int height = map[0].length;
        Color[][] portion = new Color[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = SColor.LIMITED_PALETTE[2];
                        break;
                    case '.':
                    case ':':
                        portion[i][j] = SColor.LIMITED_PALETTE[3];
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = SColor.LIMITED_PALETTE[4];
                        break;
                    case ',':
                    case '~':
                        portion[i][j] = SColor.LIMITED_PALETTE[5];
                        break;
                    case '"':
                        portion[i][j] = SColor.LIMITED_PALETTE[20];
                        break;
                    case '^':
                        portion[i][j] = SColor.LIMITED_PALETTE[6];
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            portion[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            portion[i][j] = shallowColor;
                        else portion[i][j] = SColor.LIMITED_PALETTE[1];
                }
            }
        }
        return portion;
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way; {@link SparseLayers} primarily uses this kind of packed float
     * to represent colors, and {@link SquidPanel} uses it internally.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.
     *
     * @param map a char[][] containing foreground characters
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] generateDefaultColorsFloat(char[][] map) {
        return fillDefaultColorsFloat(new float[map.length][map[0].length], map);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default colors for the usual
     * meanings of the chars in map. Each float represents a color in an efficient way; {@link SparseLayers} primarily
     * uses this kind of packed float to represent colors, and {@link SquidPanel} uses it internally.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.
     *
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] fillDefaultColorsFloat(float[][] packed, char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float wall = SColor.LIMITED_PALETTE[2].toFloatBits(),
                ground = SColor.LIMITED_PALETTE[3].toFloatBits(),
                door = SColor.LIMITED_PALETTE[4].toFloatBits(),
                water = SColor.LIMITED_PALETTE[5].toFloatBits(),
                grass = SColor.LIMITED_PALETTE[20].toFloatBits(),
                trap = SColor.LIMITED_PALETTE[6].toFloatBits(),
                other = SColor.LIMITED_PALETTE[1].toFloatBits();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        packed[i][j] = wall;
                        break;
                    case '.':
                    case ':':
                        packed[i][j] = ground;
                        break;
                    case '+':
                    case '/':
                        packed[i][j] = door;
                        break;
                    case ',':
                    case '~':
                        packed[i][j] = water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    case '^':
                        packed[i][j] = trap;
                        break;
                    default:
                        packed[i][j] = other;
                }
            }
        }
        return packed;
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way; {@link SparseLayers} primarily uses this kind of packed float
     * to represent colors, and {@link SquidPanel} uses it internally. This overload also takes a char that corresponds
     * to deep non-water lakes (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a
     * packed float color to use for that deep liquid, as well as a char for shallow lakes and another packed float
     * color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.  Deep and shallow lakes of non-water will use the given
     * packed float color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you
     * don't need this overload, and {@link #generateDefaultColorsFloat(char[][])} will be fine.
     *
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] generateDefaultColorsFloat(char[][] map, char deepChar, float deepColor,
                                                       char shallowChar, float shallowColor) {
        return fillDefaultColorsFloat(new float[map.length][map[0].length], map, deepChar, deepColor, shallowChar, shallowColor);
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way; {@link SparseLayers} primarily uses this kind of packed float
     * to represent colors, and {@link SquidPanel} uses it internally. This overload also takes a char that corresponds
     * to deep non-water lakes (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a
     * packed float color to use for that deep liquid, as well as a char for shallow lakes and another packed float
     * color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.  Deep and shallow lakes of non-water will use the given
     * packed float color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you
     * don't need this overload, and {@link #generateDefaultColorsFloat(char[][])} will be fine.
     *
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] fillDefaultColorsFloat(float[][] packed, char[][] map, char deepChar, float deepColor,
                                                   char shallowChar, float shallowColor) {
        int width = map.length;
        int height = map[0].length;
        float wall = SColor.LIMITED_PALETTE[2].toFloatBits(),
                ground = SColor.LIMITED_PALETTE[3].toFloatBits(),
                door = SColor.LIMITED_PALETTE[4].toFloatBits(),
                water = SColor.LIMITED_PALETTE[5].toFloatBits(),
                grass = SColor.LIMITED_PALETTE[20].toFloatBits(),
                trap = SColor.LIMITED_PALETTE[6].toFloatBits(),
                other = SColor.LIMITED_PALETTE[1].toFloatBits();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        packed[i][j] = wall;
                        break;
                    case '.':
                    case ':':
                        packed[i][j] = ground;
                        break;
                    case '+':
                    case '/':
                        packed[i][j] = door;
                        break;
                    case ',':
                    case '~':
                        packed[i][j] = water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    case '^':
                        packed[i][j] = trap;
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            packed[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            packed[i][j] = shallowColor;
                        else packed[i][j] = other;
                }
            }
        }
        return packed;
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way and the version this uses can be altered by the specified hue,
     * saturation, and value changes; {@link SparseLayers} primarily uses this kind of packed float to represent colors,
     * and {@link SquidPanel} uses it internally.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.
     *
     * @param map a char[][] containing foreground characters
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] generateDefaultColorsFloat(char[][] map, final float hueChange, final float saturationChange, final float valueChange) {
        return fillDefaultColorsFloat(new float[map.length][map[0].length], map, hueChange, saturationChange, valueChange);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default colors for the usual
     * meanings of the chars in map. Each float represents a color in an efficient way and the version this uses can be
     * altered by the specified hue, saturation, and value changes; {@link SparseLayers} primarily uses this kind of
     * packed float to represent colors, and {@link SquidPanel} uses it internally.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.
     *
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] fillDefaultColorsFloat(float[][] packed, char[][] map, final float hueChange, final float saturationChange, final float valueChange) {
        int width = map.length;
        int height = map[0].length;
        float wall = SColor.LIMITED_PALETTE[2].toEditedFloat(hueChange, saturationChange, valueChange),
                ground = SColor.LIMITED_PALETTE[3].toEditedFloat(hueChange, saturationChange, valueChange),
                door = SColor.LIMITED_PALETTE[4].toEditedFloat(hueChange, saturationChange, valueChange),
                water = SColor.LIMITED_PALETTE[5].toEditedFloat(hueChange, saturationChange, valueChange),
                grass = SColor.LIMITED_PALETTE[20].toEditedFloat(hueChange, saturationChange, valueChange),
                trap = SColor.LIMITED_PALETTE[6].toEditedFloat(hueChange, saturationChange, valueChange),
                other = SColor.LIMITED_PALETTE[1].toEditedFloat(hueChange, saturationChange, valueChange);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        packed[i][j] = wall;
                        break;
                    case '.':
                    case ':':
                        packed[i][j] = ground;
                        break;
                    case '+':
                    case '/':
                        packed[i][j] = door;
                        break;
                    case ',':
                    case '~':
                        packed[i][j] = water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    case '^':
                        packed[i][j] = trap;
                        break;
                    default:
                        packed[i][j] = other;
                }
            }
        }
        return packed;
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way and the version this uses can be altered by the specified hue,
     * saturation, and value changes; {@link SparseLayers} primarily uses this kind of packed float
     * to represent colors, and {@link SquidPanel} uses it internally. This overload also takes a char that corresponds
     * to deep non-water lakes (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a
     * packed float color to use for that deep liquid, as well as a char for shallow lakes and another packed float
     * color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.  Deep and shallow lakes of non-water will use the given
     * packed float color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you
     * don't need this overload, and {@link #generateDefaultColorsFloat(char[][])} will be fine.
     *
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] generateDefaultColorsFloat(char[][] map, char deepChar, float deepColor,
                                                       char shallowChar, float shallowColor,
                                                       final float hueChange, final float saturationChange, final float valueChange) {
        return fillDefaultColorsFloat(new float[map.length][map[0].length], map, deepChar, deepColor, shallowChar, shallowColor, hueChange, saturationChange, valueChange);
    }
    /**
     * Produces a float[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * Each float represents a color in an efficient way and the version this uses can be altered by the specified hue,
     * saturation, and value changes; {@link SparseLayers} primarily uses this kind of packed float
     * to represent colors, and {@link SquidPanel} uses it internally. This overload also takes a char that corresponds
     * to deep non-water lakes (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a
     * packed float color to use for that deep liquid, as well as a char for shallow lakes and another packed float
     * color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}; if that field is changed then the colors this returns
     * will also change. Tiles containing nothing but a floor ('.') will be silver-gray; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[3]}. Walls are brownish-black, and can be '#' marks or box-drawing lines; this can
     * be changed by editing {@code SColor.LIMITED_PALETTE[2]}.Doors ('+' and '/' in the map) will be rust brown; this
     * can be changed by editing {@code SColor.LIMITED_PALETTE[4]}. Both shallow and deep water (',' and '~') will be
     * gray-blue; this can be changed by editing {@code SColor.LIMITED_PALETTE[5]}. Traps ('^') will be shown in bright
     * orange; this can be changed by editing {@code SColor.LIMITED_PALETTE[6]}. Grass ('"') will be the expected green;
     * this can be changed by editing {@code SColor.LIMITED_PALETTE[20]}. Anything else will use white; this can be
     * changed by editing {@code SColor.LIMITED_PALETTE[1]}.  Deep and shallow lakes of non-water will use the given
     * packed float color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you
     * don't need this overload, and {@link #generateDefaultColorsFloat(char[][])} will be fine.
     *
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D float array with the same size as map, containing packed floats that can be used for the corresponding chars
     */
    public static float[][] fillDefaultColorsFloat(float[][] packed, char[][] map, char deepChar, float deepColor,
                                                   char shallowChar, float shallowColor,
                                                   final float hueChange, final float saturationChange, final float valueChange) {
        int width = map.length;
        int height = map[0].length;
        float wall = SColor.LIMITED_PALETTE[2].toEditedFloat(hueChange, saturationChange, valueChange),
                ground = SColor.LIMITED_PALETTE[3].toEditedFloat(hueChange, saturationChange, valueChange),
                door = SColor.LIMITED_PALETTE[4].toEditedFloat(hueChange, saturationChange, valueChange),
                water = SColor.LIMITED_PALETTE[5].toEditedFloat(hueChange, saturationChange, valueChange),
                grass = SColor.LIMITED_PALETTE[20].toEditedFloat(hueChange, saturationChange, valueChange),
                trap = SColor.LIMITED_PALETTE[6].toEditedFloat(hueChange, saturationChange, valueChange),
                other = SColor.LIMITED_PALETTE[1].toEditedFloat(hueChange, saturationChange, valueChange);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        packed[i][j] = wall;
                        break;
                    case '.':
                    case ':':
                        packed[i][j] = ground;
                        break;
                    case '+':
                    case '/':
                        packed[i][j] = door;
                        break;
                    case ',':
                    case '~':
                        packed[i][j] = water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    case '^':
                        packed[i][j] = trap;
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            packed[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            packed[i][j] = shallowColor;
                        else packed[i][j] = other;
                }
            }
        }
        return packed;
    }

    /**
     * Produces a Color[][] that corresponds to appropriate default background colors for the usual meanings of the
     * chars in map. This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black. but deep water ('~') will be dark
     * blue-green, shallow water (',') will be a lighter blue-green, and grass ('"') will be dark green.
     * You can adjust the brightness of the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you
     * want water and grass to ripple, you can use the overload {@link #generateLightnessModifiers(char[][], double)}
     * with some rising frame count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static Color[][] generateDefaultBGColors(char[][] map) {

        int width = map.length;
        int height = map[0].length;
        Color[][] portion = new Color[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case '.':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case ':':
                        portion[i][j] = SColor.LIMITED_PALETTE[35];
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case ',':
                        portion[i][j] = SColor.LIMITED_PALETTE[23];
                        break;
                    case '~':
                        portion[i][j] = SColor.LIMITED_PALETTE[24];
                        break;
                    case '"':
                        portion[i][j] = SColor.LIMITED_PALETTE[21];
                        break;
                    case '^':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    default:
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                }
            }
        }
        return portion;
    }


    /**
     * Produces a Color[][] that corresponds to appropriate default background colors for the usual meanings of the
     * chars in map. This overload also takes a char that corresponds to deep non-water lakes (which
     * {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a Color to use for that deep liquid,
     * as well as a char for shallow lakes and a Color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black, but deep water ('~') will be dark
     * blue-green, shallow water (',') will be a lighter blue-green, and grass ('"') will be dark green.
     * Deep and shallow lakes of non-water will use the given Colors. If you are using SectionDungeonGenerator to
     * produce normal water lakes, then you don't need this overload, and
     * {@link #generateDefaultBGColors(char[][])} will be fine.
     * You can adjust the brightness of the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you
     * want water, grass, and lakes to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the Color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the Color to use for shallow parts of non-water lakes
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static Color[][] generateDefaultBGColors(char[][] map, char deepChar, Color deepColor,
                                                    char shallowChar, Color shallowColor) {

        int width = map.length;
        int height = map[0].length;
        Color[][] portion = new Color[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case '.':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case ':':
                        portion[i][j] = SColor.LIMITED_PALETTE[35];
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    case ',':
                        portion[i][j] = SColor.LIMITED_PALETTE[23];
                        break;
                    case '~':
                        portion[i][j] = SColor.LIMITED_PALETTE[24];
                        break;
                    case '"':
                        portion[i][j] = SColor.LIMITED_PALETTE[21];
                        break;
                    case '^':
                        portion[i][j] = SColor.LIMITED_PALETTE[0];
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            portion[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            portion[i][j] = shallowColor;
                        else portion[i][j] = SColor.LIMITED_PALETTE[0];
                }
            }
        }
        return portion;
    }

    /**
     * Produces a float[][] of packed float colors that corresponds to appropriate default background colors for the
     * usual meanings of the chars in map.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. You can adjust the brightness of the backgrounds using
     * {@link #generateLightnessModifiers(char[][])}, or if you want water and grass to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame or millisecond count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] generateDefaultBGColorsFloat(char[][] map) {
        return fillDefaultBGColorsFloat(new float[map.length][map[0].length], map);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default background colors for
     * the usual meanings of the chars in map. The sizes of map and packed should be identical.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. You can adjust the brightness of the backgrounds using
     * {@link #generateLightnessModifiers(char[][])}, or if you want water and grass to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame or millisecond count.
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] fillDefaultBGColorsFloat(float[][] packed, char[][] map) {
        int width = map.length;
        int height = map[0].length;
        float   bridge = SColor.LIMITED_PALETTE[35].toFloatBits(),
                shallow_water = SColor.LIMITED_PALETTE[23].toFloatBits(),
                deep_water = SColor.LIMITED_PALETTE[24].toFloatBits(),
                grass = SColor.LIMITED_PALETTE[21].toFloatBits(),
                other = SColor.LIMITED_PALETTE[0].toFloatBits();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ':':
                        packed[i][j] = bridge;
                        break;
                    case ',':
                        packed[i][j] = shallow_water;
                        break;
                    case '~':
                        packed[i][j] = deep_water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    default:
                        packed[i][j] = other;
                }
            }
        }
        return packed;
    }


    /**
     * Produces a float[][] of packed float colors that corresponds to appropriate default background colors for the
     * usual meanings of the chars in map. This overload also takes a char that corresponds to deep non-water lakes
     * (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a packed float color to use
     * for that deep liquid,  as well as a char for shallow lakes and a packed float color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. Deep and shallow lakes of non-water will use the given packed float
     * color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you don't need
     * this overload, and {@link #generateDefaultBGColorsFloat(char[][])} will be fine. You can adjust the brightness of
     * the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you want water, grass, and lakes to
     * ripple, you can use the overload {@link #generateLightnessModifiers(char[][], double, char, char)} with some
     * rising frame or millisecond count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @return a 2D array of background packed float colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] generateDefaultBGColorsFloat(char[][] map, char deepChar, float deepColor,
                                                         char shallowChar, float shallowColor) {
        return fillDefaultBGColorsFloat(new float[map.length][map[0].length], map, deepChar, deepColor, shallowChar, shallowColor);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default background colors for
     * the usual meanings of the chars in map. This overload also takes a char that corresponds to deep non-water lakes
     * (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a packed float color to use
     * for that deep liquid,  as well as a char for shallow lakes and a packed float color for that shallow liquid. The
     * sizes of packed and map should be identical.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. Deep and shallow lakes of non-water will use the given packed float
     * color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you don't need
     * this overload, and {@link #generateDefaultBGColorsFloat(char[][])} will be fine. You can adjust the brightness of
     * the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you want water, grass, and lakes to
     * ripple, you can use the overload {@link #generateLightnessModifiers(char[][], double, char, char)} with some
     * rising frame or millisecond count.
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @return a 2D array of background packed float colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] fillDefaultBGColorsFloat(float[][] packed, char[][] map, char deepChar, float deepColor,
                                                     char shallowChar, float shallowColor) {
        int width = map.length;
        int height = map[0].length;
        float   bridge = SColor.LIMITED_PALETTE[35].toFloatBits(),
                shallow_water = SColor.LIMITED_PALETTE[23].toFloatBits(),
                deep_water = SColor.LIMITED_PALETTE[24].toFloatBits(),
                grass = SColor.LIMITED_PALETTE[21].toFloatBits(),
                other = SColor.LIMITED_PALETTE[0].toFloatBits();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ':':
                        packed[i][j] = bridge;
                        break;
                    case ',':
                        packed[i][j] = shallow_water;
                        break;
                    case '~':
                        packed[i][j] = deep_water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            packed[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            packed[i][j] = shallowColor;
                        else packed[i][j] = other;
                }
            }
        }
        return packed;
    }

    /**
     * Produces a float[][] of packed float colors that corresponds to appropriate default background colors for the
     * usual meanings of the chars in map. Each float represents a color in an efficient way and the version this uses
     * can be altered by the specified hue, saturation, and value changes.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. You can adjust the brightness of the backgrounds using
     * {@link #generateLightnessModifiers(char[][])}, or if you want water and grass to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame or millisecond count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] generateDefaultBGColorsFloat(char[][] map, final float hueChange, final float saturationChange, final float valueChange) {
        return fillDefaultBGColorsFloat(new float[map.length][map[0].length], map, hueChange, saturationChange, valueChange);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default background colors for
     * the usual meanings of the chars in map. Each float represents a color in an efficient way and the version this
     * uses can be altered by the specified hue, saturation, and value changes. The sizes of map and packed should be
     * identical.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. You can adjust the brightness of the backgrounds using
     * {@link #generateLightnessModifiers(char[][])}, or if you want water and grass to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame or millisecond count.
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D array of background Colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] fillDefaultBGColorsFloat(float[][] packed, char[][] map, final float hueChange, final float saturationChange, final float valueChange) {
        int width = map.length;
        int height = map[0].length;
        float   bridge = SColor.LIMITED_PALETTE[35].toEditedFloat(hueChange, saturationChange, valueChange),
                shallow_water = SColor.LIMITED_PALETTE[23].toEditedFloat(hueChange, saturationChange, valueChange),
                deep_water = SColor.LIMITED_PALETTE[24].toEditedFloat(hueChange, saturationChange, valueChange),
                grass = SColor.LIMITED_PALETTE[21].toEditedFloat(hueChange, saturationChange, valueChange),
                other = SColor.LIMITED_PALETTE[0].toEditedFloat(hueChange, saturationChange, valueChange);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ':':
                        packed[i][j] = bridge;
                        break;
                    case ',':
                        packed[i][j] = shallow_water;
                        break;
                    case '~':
                        packed[i][j] = deep_water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    default:
                        packed[i][j] = other;
                }
            }
        }
        return packed;
    }


    /**
     * Produces a float[][] of packed float colors that corresponds to appropriate default background colors for the
     * usual meanings of the chars in map. Each float represents a color in an efficient way and the version this uses
     * can be altered by the specified hue, saturation, and value changes. This overload also takes a char that
     * corresponds to deep non-water lakes
     * (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a packed float color to use
     * for that deep liquid,  as well as a char for shallow lakes and a packed float color for that shallow liquid.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. Deep and shallow lakes of non-water will use the given packed float
     * color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you don't need
     * this overload, and {@link #generateDefaultBGColorsFloat(char[][])} will be fine. You can adjust the brightness of
     * the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you want water, grass, and lakes to
     * ripple, you can use the overload {@link #generateLightnessModifiers(char[][], double, char, char)} with some
     * rising frame or millisecond count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D array of background packed float colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] generateDefaultBGColorsFloat(char[][] map, char deepChar, float deepColor,
                                                         char shallowChar, float shallowColor,
                                                         final float hueChange, final float saturationChange, final float valueChange) {
        return fillDefaultBGColorsFloat(new float[map.length][map[0].length], map, deepChar, deepColor, shallowChar, shallowColor, hueChange, saturationChange, valueChange);
    }
    /**
     * Fills an existing float[][] with packed float colors that correspond to appropriate default background colors for
     * the usual meanings of the chars in map. Each float represents a color in an efficient way and the version this
     * uses can be altered by the specified hue, saturation, and value changes. This overload also takes a char that
     * corresponds to deep non-water lakes
     * (which {@link squidpony.squidgrid.mapping.SectionDungeonGenerator} can produce) and a packed float color to use
     * for that deep liquid,  as well as a char for shallow lakes and a packed float color for that shallow liquid. The
     * sizes of packed and map should be identical.
     * <br>
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[0]}. Deep water ('~') will be dark blue-green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[24]}. Shallow water (',') will be a lighter blue-green; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[23]}. Grass ('"') will be dark green; this can be changed by editing
     * {@code SColor.LIMITED_PALETTE[21]}. Bridges (':') will be a medium-dark beige color; this can be changed by
     * editing {@code SColor.LIMITED_PALETTE[35]}. Deep and shallow lakes of non-water will use the given packed float
     * color parameters. If you are using SectionDungeonGenerator to produce normal water lakes, then you don't need
     * this overload, and {@link #generateDefaultBGColorsFloat(char[][])} will be fine. You can adjust the brightness of
     * the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you want water, grass, and lakes to
     * ripple, you can use the overload {@link #generateLightnessModifiers(char[][], double, char, char)} with some
     * rising frame or millisecond count.
     * @param packed a float[][] that will be modified, filled with packed float colors; must match map's size
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param deepColor the packed float color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link squidpony.squidgrid.mapping.SectionDungeonGenerator}
     * @param shallowColor  the packed float color to use for shallow parts of non-water lakes
     * @param hueChange a float from -1f to 1f that will be added to the hues of colors this uses
     * @param saturationChange a float from -1f to 1f that will be added to the saturation of colors this uses
     * @param valueChange a float from -1f to 1f that will be added to the values of colors this uses
     * @return a 2D array of background packed float colors with the same size as map, that can be used for the corresponding chars
     */
    public static float[][] fillDefaultBGColorsFloat(float[][] packed, char[][] map, char deepChar, float deepColor,
                                                     char shallowChar, float shallowColor,
                                                     final float hueChange, final float saturationChange, final float valueChange) {
        int width = map.length;
        int height = map[0].length;
        float   bridge = SColor.LIMITED_PALETTE[35].toEditedFloat(hueChange, saturationChange, valueChange),
                shallow_water = SColor.LIMITED_PALETTE[23].toEditedFloat(hueChange, saturationChange, valueChange),
                deep_water = SColor.LIMITED_PALETTE[24].toEditedFloat(hueChange, saturationChange, valueChange),
                grass = SColor.LIMITED_PALETTE[21].toEditedFloat(hueChange, saturationChange, valueChange),
                other = SColor.LIMITED_PALETTE[0].toEditedFloat(hueChange, saturationChange, valueChange);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case ':':
                        packed[i][j] = bridge;
                        break;
                    case ',':
                        packed[i][j] = shallow_water;
                        break;
                    case '~':
                        packed[i][j] = deep_water;
                        break;
                    case '"':
                        packed[i][j] = grass;
                        break;
                    default:
                        if (map[i][j] == deepChar)
                            packed[i][j] = deepColor;
                        else if (map[i][j] == shallowChar)
                            packed[i][j] = shallowColor;
                        else packed[i][j] = other;
                }
            }
        }
        return packed;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors.
     *
     * @param map a char[][] that you want to be find background lightness modifiers for
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map) {
        return fillLightnessModifiers(new int[map.length][map[0].length], map);
    }
    /**
     * Fills an existing int[][] with lighting values that can be used with SquidLayers to alter the background colors.
     *
     * @param map a char[][] that you want to be find background lightness modifiers for
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] fillLightnessModifiers(int[][] lights, char[][] map) {
        int width = map.length;
        int height = map[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        lights[i][j] = 30;
                        break;
                    case '.':
                        lights[i][j] = 0;
                        break;
                    case ':':
                        lights[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        lights[i][j] = -10;
                        break;
                    case ',':
                        lights[i][j] = -40;
                        break;
                    case '~':
                        lights[i][j] = -85;
                        break;
                    case '"':
                        lights[i][j] = -110;
                        break;
                    case '^':
                        lights[i][j] = 40;
                        break;
                    default:
                        lights[i][j] = 0;
                }
            }
        }
        return lights;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using Perlin Noise are desired.
     *
     * @param map   a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame) {
        return fillLightnessModifiers(new int[map.length][map[0].length], map, frame);
    }

    /**
     * Fills an existing int[][] with lighting values that can be used with SquidLayers to alter the background colors,
     * accepting a parameter for animation frame if rippling water and waving grass using Perlin Noise are desired.
     *
     * @param lights an int[][] that will be modified, filled with lighting ints; must match map's size
     * @param map    a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] fillLightnessModifiers(int[][] lights, char[][] map, double frame) {
        int width = map.length;
        int height = map[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        lights[i][j] = 30;
                        break;
                    case '.':
                        lights[i][j] = 0;
                        break;
                    case ':':
                        lights[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        lights[i][j] = -10;
                        break;
                    case ',':
                        lights[i][j] = (int) (85 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.55 - 0.7));
                        break;
                    case '~':
                        lights[i][j] = (int) (100 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.4 - 0.65));
                        break;
                    case '"':
                        lights[i][j] = (int) (95 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.5) * 0.3 - 1.5));
                        break;
                    case '^':
                        lights[i][j] = 40;
                        break;
                    default:
                        lights[i][j] = 0;
                }
            }
        }
        return lights;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using Perlin Noise are desired. Also allows additional chars
     * to be treated like deep and shallow liquid regarding the ripple animation.
     *
     * @param map           a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @param deepLiquid    a char that will be treated like deep water when animating ripples
     * @param shallowLiquid a char that will be treated like shallow water when animating ripples
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame, char deepLiquid, char shallowLiquid) {
        return fillLightnessModifiers(new int[map.length][map[0].length], map, frame, deepLiquid, shallowLiquid);
    }
    /**
     * Fills an existing int[][] with lighting values that can be used with SquidLayers to alter the background colors,
     * accepting a parameter for animation frame if rippling water and waving grass using Perlin Noise are desired. Also
     * allows additional chars to be treated like deep and shallow liquid regarding the ripple animation.
     *
     * @param lights an int[][] that will be modified, filled with lighting ints; must match map's size
     * @param map           a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more, and 0.013 multiplied by the current time in milliseconds works well
     *                      as long as only the smaller digits of the time are used; this can be accomplished with
     *                      {@code (System.currentTimeMillis() & 0xFFFFFF) * 0.013} .
     * @param deepLiquid    a char that will be treated like deep water when animating ripples
     * @param shallowLiquid a char that will be treated like shallow water when animating ripples
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] fillLightnessModifiers(int[][] lights, char[][] map, double frame, char deepLiquid, char shallowLiquid) {
        int width = map.length;
        int height = map[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (map[i][j]) {
                    case '\1':
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                    case '#':
                        lights[i][j] = 30;
                        break;
                    case '.':
                        lights[i][j] = 0;
                        break;
                    case ':':
                        lights[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        lights[i][j] = -10;
                        break;
                    case ',':
                        lights[i][j] = (int) (85 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.55 - 0.7));
                        break;
                    case '~':
                        lights[i][j] = (int) (100 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.45) * 0.4 - 0.65));
                        break;
                    case '"':
                        lights[i][j] = (int) (95 * (PerlinNoise.noise(i * 1.45, j * 1.45, frame * 0.5) * 0.3 - 1.5));
                        break;
                    case '^':
                        lights[i][j] = 40;
                        break;
                    default:
                        if (map[i][j] == deepLiquid)
                            lights[i][j] = (int) (180 * (PerlinNoise.noise(i * 4.2, j * 4.2, frame * 0.55) * 0.45 - 0.7));
                        else if (map[i][j] == shallowLiquid)
                            lights[i][j] = (int) (110 * (PerlinNoise.noise(i * 3.1, j * 3.1, frame * 0.3) * 0.4 - 0.65));
                        else lights[i][j] = 0;
                }
            }
        }
        return lights;
    }

    /**
     * Meant to be used with {@link TextCellFactory#draw(Batch, float[][], float, float, int, int)}, this produces a
     * triple-width, triple-height float color array by finding the box-drawing characters in {@code map} and placing
     * 3x3 boxes into that triple-size array with the matching color from {@code colors} in the shape of that
     * box-drawing character. {@code map} and {@code colors} should be the same size. Will return a new 2D float array.
     * This should usually be passed to the aforementioned TextCellFactory draw method with 3 xSubCells and 3 ySubCells.
     * An intended purpose for this is to draw box-drawing-like blocks when the font doesn't support box-drawing
     * characters or those characters don't line up correctly for any reason.
     * @param map a 2D char array that will not be modified; box drawing characters in this will be drawn in the returned array
     * @param colors a 2D float array of packed float colors to use for drawing boxes (possibly produced by
     *               {@link #fillDefaultColorsFloat(float[][], char[][])} or other methods in this class)
     * @return a 2D float array that can be drawn by {@link TextCellFactory#draw(Batch, float[][], float, float, int, int)} with 3x3 subcells
     */
    public static float[][] generateLinesToBoxes(char[][] map, float[][] colors) {
        return fillLinesToBoxes(null, map, colors);
    }

    /**
     * Meant to be used with {@link TextCellFactory#draw(Batch, float[][], float, float, int, int)}, this finds the
     * box-drawing characters in {@code map} and fills 3x3 boxes in {@code into} with the matching color from
     * {@code colors} in the shape of that box-drawing character. This means {@code into} must have at least 3 times the
     * width and height of {@code map}, and {@code map} and {@code colors} should be the same size. If {@code into} is
     * appropriately-sized, then this will modify {@code into} in-place; otherwise it will return a new 2D float array.
     * This should usually be passed to the aforementioned TextCellFactory draw method with 3 xSubCells and 3 ySubCells.
     * An intended purpose for this is to draw box-drawing-like blocks when the font doesn't support box-drawing
     * characters or those characters don't line up correctly for any reason.
     * @param into a 2D float array that will be cleared, then modified in-place and returned; should be 3x the width
     *             and 3x the height of map and colors, but if it is not (or is null) a new array will be allocated
     * @param map a 2D char array that will not be modified; box drawing characters in this will be drawn in into
     * @param colors a 2D float array of packed float colors to use for drawing boxes (possibly produced by
     *               {@link #fillDefaultColorsFloat(float[][], char[][])} or other methods in this class)
     * @return {@code into}, if modified, or a new 2D float array if into was null or incorrectly sized
     */
    public static float[][] fillLinesToBoxes(float[][] into, char[][] map, float[][] colors) {
        final int width = Math.min(map.length, colors.length);
        final int height = Math.min(map[0].length, colors[0].length);
        if(into == null || into.length < width * 3 || into[0].length < height * 3)
            into = new float[width * 3][height * 3];
        else
            ArrayTools.fill(into, 0f);
        for (int i = 0, x = 0; i < width; i++, x+=3) {
            for (int j = 0, y = 0; j < height; j++, y+=3) {
                switch (map[i][j]) {
                    case '\1':
                    case '#':
                        into[x][y] = into[x+1][y] = into[x+2][y] =
                                into[x][y+1] = into[x+1][y+1] = into[x+2][y+1] =
                                        into[x][y+2] = into[x+1][y+2] = into[x+2][y+2] = colors[i][j];
                        break;
                    case '├':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            /*into[x][y+1] =*/ into[x+1][y+1] = into[x+2][y+1] =
                            /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┤':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            into[x][y+1] = into[x+1][y+1] = /*into[x+2][y+1] =*/
                                    /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┴':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            into[x][y+1] = into[x+1][y+1] = into[x+2][y+1] =
                                    /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┬':
                        /*into[x][y] = into[x+1][y] = into[x+2][y] =*/
                        into[x][y+1] = into[x+1][y+1] = into[x+2][y+1] =
                                /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┌':
                        /*into[x][y] = into[x+1][y] = into[x+2][y] =*/
                        /*into[x][y+1] =*/ into[x+1][y+1] = into[x+2][y+1] =
                            /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┐':
                        /*into[x][y] = into[x+1][y] = into[x+2][y] =*/
                        into[x][y+1] = into[x+1][y+1] = /*into[x+2][y+1] =*/
                                /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '└':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            /*into[x][y+1] =*/ into[x+1][y+1] = into[x+2][y+1] =
                            /*into[x][y+2] = into[x+1][y+2] = into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┘':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            into[x][y+1] = into[x+1][y+1] = /*into[x+2][y+1] =*/
                                    /*into[x][y+2] = into[x+1][y+2] = into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '│':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            /*into[x][y+1] =*/ into[x+1][y+1] = /*into[x+2][y+1] =*/
                            /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '─':
                        into[x][y+1] = into[x+1][y+1] = into[x+2][y+1] = colors[i][j];
                        break;
                    case '╴':
                        into[x][y+1] = into[x+1][y+1] = colors[i][j];
                        break;
                    case '╵':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            /*into[x][y+1] =*/ into[x+1][y+1] = /*into[x+2][y+1] =*/ colors[i][j];
                        break;
                    case '╶':
                        into[x+1][y+1] = into[x+2][y+1] = colors[i][j];
                        break;
                    case '╷':
                            /*into[x][y+1] =*/ into[x+1][y+1] = /*into[x+2][y+1] =*/
                            /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                    case '┼':
                        /*into[x][y] =*/ into[x+1][y] = /*into[x+2][y] =*/
                            into[x][y+1] = into[x+1][y+1] = into[x+2][y+1] =
                                    /*into[x][y+2] =*/ into[x+1][y+2] = /*into[x+2][y+2] =*/ colors[i][j];
                        break;
                }
            }
        }
        return into;
    }

}
