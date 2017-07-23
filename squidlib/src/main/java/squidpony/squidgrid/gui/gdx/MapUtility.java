package squidpony.squidgrid.gui.gdx;

import com.badlogic.gdx.graphics.Color;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.SeededNoise;

/**
 * Created by Tommy Ettinger on 7/9/2017.
 */
public class MapUtility {
    /**
     * Produces a Color[][] that corresponds to appropriate default colors for the usual meanings of the chars in map.
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the colors this
     * returns will also change. Tiles containing nothing but a floor ('.') will be silver-gray. Walls are dark gray
     * (this doesn't care if the walls are '#' marks or box-drawing lines), Doors ('+' and '/' in the map) will be rust
     * brown. Both shallow and deep water (',' and '~') will be gray-blue. Traps ('^') will be shown in bright orange.
     * Grass ('"') will be the expected green.
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
     * This overload also takes a char that corresponds to deep non-water lakes (which {@link SectionDungeonGenerator}
     * can produce) and a Color to use for that deep liquid, as well as a char for shallow lakes and a Color for that
     * shallow liquid.
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the colors this
     * returns will also change. Tiles containing nothing but a floor ('.') will be silver-gray. Walls are dark gray
     * (this doesn't care if the walls are '#' marks or box-drawing lines), Doors ('+' and '/' in the map) will be rust
     * brown. Both deep and shallow water (',' and '~') will be gray-blue. Traps ('^') will be shown in bright orange.
     * Grass ('"') will be the expected green. Deep and shallow lakes of non-water will use the given Colors. If you
     * are using SectionDungeonGenerator to produce normal water lakes, then you don't need this overload, and
     * {@link #generateDefaultColors(char[][])} will be fine.
     *
     * @param map a char[][] containing foreground characters
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link SectionDungeonGenerator}
     * @param deepColor the Color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link SectionDungeonGenerator}
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
     * {@link SectionDungeonGenerator} can produce) and a Color to use for that deep liquid, as well as a char for
     * shallow lakes and a Color for that shallow liquid.
     * This takes its values from {@link SColor#LIMITED_PALETTE}, and if that field is changed then the
     * colors this returns will also change. Most backgrounds will be black. but deep water ('~') will be dark
     * blue-green, shallow water (',') will be a lighter blue-green, and grass ('"') will be dark green.
     * Deep and shallow lakes of non-water will use the given Colors. If you are using SectionDungeonGenerator to
     * produce normal water lakes, then you don't need this overload, and
     * {@link #generateDefaultBGColors(char[][])} will be fine.
     * You can adjust the brightness of the backgrounds using {@link #generateLightnessModifiers(char[][])}, or if you
     * want water, grass, and lakes to ripple, you can use the overload
     * {@link #generateLightnessModifiers(char[][], double, char, char)} with some rising frame count.
     *
     * @param map a char[][] containing foreground characters (this gets their background color)
     * @param deepChar the char that represents deep parts of non-water lakes, from {@link SectionDungeonGenerator}
     * @param deepColor the Color to use for deep parts of non-water lakes
     * @param shallowChar the char that represents shallow parts of non-water lakes, from {@link SectionDungeonGenerator}
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
     * Produces an int[][] that can be used with SquidLayers to alter the background colors.
     *
     * @param map a char[][] that you want to be find background lightness modifiers for
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
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
                        portion[i][j] = 30;
                        break;
                    case '.':
                        portion[i][j] = 0;
                        break;
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887) / 2.5 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887) / 2.5 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (75 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887) / 4.0 - 1.5));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        portion[i][j] = 0;
                }
            }
        }
        return portion;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using {@link SeededNoise} are desired. It may make sense to
     * pass some fraction of the current time, as given by {@link System#currentTimeMillis()}, instead of a frame.
     *
     * @param map   a char[][] that you want to be find background lightness modifiers for
     * @param frame a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *              water and grass move more
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
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
                        portion[i][j] = 30;
                        break;
                    case '.':
                        portion[i][j] = 0;
                        break;
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.40231) / 2.5 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.40231) / 2.5 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (75 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.45231) / 4.0 - 1.5));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        portion[i][j] = 0;
                }
            }
        }
        return portion;
    }

    /**
     * Produces an int[][] that can be used with SquidLayers to alter the background colors, accepting a parameter for
     * animation frame if rippling water and waving grass using {@link SeededNoise} are desired. It may make sense to
     * pass some fraction of the current time, as given by {@link System#currentTimeMillis()}, instead of a frame.
     * Also allows additional chars to be treated like deep and shallow water regarding the ripple animation.
     *
     * @param map           a char[][] that you want to be find background lightness modifiers for
     * @param frame         a counter that typically should increase by between 10.0 and 20.0 each second; higher numbers make
     *                      water and grass move more
     * @param deepLiquid    a char that will be treated like deep water when animating ripples
     * @param shallowLiquid a char that will be treated like shallow water when animating ripples
     * @return a 2D array of lightness values from -255 to 255 but usually close to 0; can be passed to SquidLayers
     */
    public static int[][] generateLightnessModifiers(char[][] map, double frame, char deepLiquid, char shallowLiquid) {
        int width = map.length;
        int height = map[0].length;
        int[][] portion = new int[width][height];
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
                        portion[i][j] = 30;
                        break;
                    case '.':
                        portion[i][j] = 0;
                        break;
                    case ':':
                        portion[i][j] = -15;
                        break;
                    case '+':
                    case '/':
                        portion[i][j] = -10;
                        break;
                    case ',':
                        portion[i][j] = (int) (70 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.40231) / 2.5 - 0.45));
                        break;
                    case '~':
                        portion[i][j] = (int) (100 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.40231) / 2.5 - 0.65));
                        break;
                    case '"':
                        portion[i][j] = (int) (75 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame * 0.45231) / 4.0 - 1.5));
                        break;
                    case '^':
                        portion[i][j] = 40;
                        break;
                    default:
                        if (map[i][j] == deepLiquid)
                            portion[i][j] = (int) (180 * (PerlinNoise.noise(i * 1.2, j * 1.2, frame / 21.0) / 2.5 - 0.7));
                        else if (map[i][j] == shallowLiquid)
                            portion[i][j] = (int) (110 * (PerlinNoise.noise(i* 1.6180339887, j* 1.6180339887, frame / 30.0) / 2.5 - 0.45));
                        else portion[i][j] = 0;
                }
            }
        }
        return portion;
    }
}
