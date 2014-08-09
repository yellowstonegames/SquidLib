package squidpony.examples.libgdxExample;

import java.awt.Font;
import squidpony.squidcolor.SColor;
import squidpony.squidgrid.libgdx.SGPanelGDX;

/**
 * Contains links to everything in the current game world.
 *
 * @author Eben
 */
public class World {

    //
    public Map overworld;
    public Item player;
    private Map currentMap;
    private static final String[] DEFAULT_MAP = new String[]{//in order to be in line with GUI coordinate pairs, this appears to be sideways in this style constructor.
        "øøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøøø########################################øøøøøøøøøøøøøøøøøøøøøøøøø",
        "øøøøøøøøøøøøøøøøøø#########øøøøøøøø#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#..m.mmmmmmmmmmmmmm..m...ø",
        "øøøøøøøøøøø########.......##øøøøøøø#,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,#.mmTmmmmmmmmmmmmmm......ø",
        "øøøøø#######.......₤.......###øøøøø#¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸,TTT¸,,¸¸¸¸¸¸m.TmmmmmmmmmTmmmmmmm..m..ø",
        "øøø###₤₤₤₤₤..................#øøøøø#¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸¸,TTT,,¸¸¸¸¸¸mmmmm≈≈≈mm..mmmmmmmmm....ø",
        "øøø#₤₤₤₤₤.₤₤....₤............##øøøø#¸¸¸¸¸¸¸¸¸¸¸TTT¸¸¸¸¸¸¸¸¸¸¸¸¸,¸¸,,¸¸¸¸¸¸mmm≈≈≈≈≈mm.m.mmmmmmm.....ø",
        "øø##.₤₤₤₤₤₤₤₤.................####ø#¸¸¸¸¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸¸¸c,¸,,¸¸¸¸¸¸mm≈≈m≈mmmmmmmmm≈≈≈m..m...ø",
        "øø#..₤₤₤₤₤₤₤.....................###¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸¸¸ct¸ctc,,,¸¸¸¸¸¸m≈≈mmmmmmmTmmm≈≈≈≈≈≈≈≈...ø",
        "øø#...₤₤₤₤₤............₤............¸¸¸¸¸TTTTTT¸¸¸¸¸¸¸¸¸¸¸¸ctt¸c¸¸,,¸¸¸¸¸¸mm≈≈mmmmmTmmm≈≈≈m≈≈mmmm..ø",
        "øø#.₤₤₤₤₤₤₤...........₤₤............¸¸TTTTT¸¸TTT¸¸¸¸¸¸¸¸¸¸¸¸cc¸¸¸¸,,¸¸¸¸¸¸mmm≈mmmmmmmmm≈≈mTm≈≈mmm..ø",
        "ø##₤₤₤₤₤₤₤₤₤.......................###############################,,¸¸¸¸S¸#mmmm≈m≈≈≈mm≈≈≈≈mmmmm≈mmmø",
        "ø#₤₤₤₤₤₤₤₤₤₤..₤....................#.....#.....#.....#.....#.....#+/#¸¸¸S¸#.T.mm≈≈≈≈≈≈≈≈≈mmmTmmmmm.ø",
        "ø#₤₤₤₤..₤₤₤₤₤......................#.....#.....#.....#.....#.....#..#¸¸¸¸¸#.TT.mmm≈≈≈≈≈≈≈≈mmm.mmT.mø",
        "ø#.₤₤₤.₤₤₤₤₤₤......₤...............#.....#.....#.....#.....#.....#..#######.TTTTmmm≈≈≈≈≈≈mmT..mmm.mø",
        "ø#₤₤₤₤₤₤₤₤₤........₤...............#.....#.....#.....#.....#.....#/+#.....#..TTmmmm≈≈≈≈≈≈TTmTmmTmmmø",
        "ø#₤₤₤₤₤₤₤₤₤₤.......................#.....#.....#.....#.....#.....#..#.....#..T.mmmm≈≈≈≈≈≈≈mmTmTm≈≈≈ø",
        "ø#₤₤₤₤₤₤₤₤₤₤.......................#.....###+#####+#####/#####+###..+.....#...Tmmmmm≈≈≈≈≈≈≈≈mmmmT≈≈ø",
        "##₤₤₤₤₤₤₤₤₤₤.......................#######..........................#.....#mT..mmmmmm≈≈≈≈≈≈≈mmm≈≈≈mø",
        "#₤₤..₤₤₤₤₤₤₤₤......................#.....#..........................#.....#m..mmmmmmm≈≈≈≈≈≈≈Tm≈≈≈mmø",
        "#₤..₤₤₤₤₤₤₤₤₤......................#.....#..........................#######..mmmmmmmmm≈≈≈≈≈≈m≈≈mmmmø",
        "#..................................#.....#...####################...#...#E#..mmm.mmmmm≈≈≈≈≈≈≈≈mmmmmø",
        "#..................................#.....#...+..E#..............#.../.../.#.......mmmm≈≈≈≈≈mmmmmmmmø",
        "#..................................#.....#...#####..............#...#...#E#........mm≈≈≈≈≈mmmmmmmmmø",
        "#..................................#.....#...#..................#...#######...m.....m≈≈≈≈mmmmmmmmmmø",
        "#..................................#.....#...#..................#.........+......mmmm≈≈mmm....mm≈≈mø",
        "#..................................#.....#...#..................#.........+...uu...um≈≈mu.....m≈≈≈mø",
        "#..................................#.....#...#..................#...#+###+#..uuuuuuuu≈≈uu.u.ummmmuuø",
        "#..................................#.....#...#.................##...#..#c.#uuuuuuuuuu≈≈uuuAuuuuuuuuø",
        "#..................................#.....#...#................#.#...#E.#t.#uuuuuAuuA≈≈≈≈≈uuuuuuuuuuø",
        "#..................................#.....#...#...............#..#...#E<#c.#uuAuAuuu≈≈≈≈≈≈≈AuAAuuAuuø",
        "#..................................#.....#...#.............##...#...#######uAuAAA≈≈≈≈≈≈≈≈≈≈AAAAAAAuø",
        "#..................................#.....#...#............#.....#...#.....#AAAuA≈≈≈≈≈≈≈≈≈≈≈AAAAAAAAø",
        "#..................................#.....#...#............#.....#...#.....#AAAA≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAAAø",
        "#..................................#.....#...####################...#.....#AAAAu≈≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAø",
        "#..................................#.....#.......EEEEEEEEEEE........#.....#AAAAuu.≈≈≈≈mmm≈≈≈≈≈AAuAAø",
        "#..................................#.....#..........................#.....#AAAuuuu≈≈≈≈≈mm≈≈≈≈AAuuAAø",
        "#..................................#.....#..........................#.....#AAAAuuuu≈≈≈≈≈≈≈≈≈AAuuuAAø",
        "#..................................#.....####+###+#####+#####+#####+#.....#AAAAAAAuu..≈≈≈≈≈AAAAuAAAø",
        "#..................................#.....#E.+.#.....#.....#.....#.........#AAAAAAAAA.AAA≈≈AAAAAAAAAø",
        "#..................................#.....####.#.....#.....#.....#tttt+#...#AAAAAAAA..AAAuuu.uAAAAAAø",
        "#..................................#.....#E.+.#.....#.....#.....#..c..#...#AAAAAAA....AAAAu..uAAAAAø",
        "#..................................#.....####.#.....#.....#.....###..E#...#AAAAAA...AAAAAuuu.uAAAAAø",
        "#..................................#.....#E.+.#.....#.....#.....#E+.EE#...#AAAAAAAAAAAAAAAAAuuAAAAAø",
        "###########################################################################AAAAAAAAAAAAAAAAAAAAAAAAø"
    };

    public void outputMap(SGPanelGDX pane) {
        int width = currentMap.width;
        int height = currentMap.height;
        pane.initialize(width, height, new Font(Font.SERIF, Font.PLAIN, 18));

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Item tile = currentMap.contents[x][y];
                pane.placeCharacter(x, y, tile.symbol, tile.color);
            }
        }
        
        
    }

    public Map getDefaultMap() {
        int width = DEFAULT_MAP[0].length();
        int height = DEFAULT_MAP.length;
        Item[][] tiles = new Item[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char c = DEFAULT_MAP[y].charAt(x);
                SColor color;
                switch (c) {
                    case '.'://stone ground
                        color = SColor.SLATE_GRAY;
                        break;
                    case '¸'://grass
                        color = SColor.GREEN;
                        break;
                    case ','://pathway
                        color = SColor.STOREROOM_BROWN;
                        c = '.';
                        break;
                    case 'c':
                        color = SColor.SEPIA;
                        break;
                    case '/':
                        color = SColor.BROWNER;
                        break;
                    case '≈':
                        color = SColor.AZUL;
                        break;
                    case '<':
                    case '>':
                        color = SColor.SLATE_GRAY;
                        break;
                    case 't':
                        color = SColor.BROWNER;
                        break;
                    case 'm':
                        color = SColor.BAIKO_BROWN;
                        break;
                    case 'u':
                        color = SColor.TAN;
                        break;
                    case 'T':
                    case '₤':
                        color = SColor.FOREST_GREEN;
                        break;
                    case 'E':
                        color = SColor.SILVER;
                        break;
                    case 'S':
                        color = SColor.BREWED_MUSTARD_BROWN;
                        break;
                    case '#':
                        color = SColor.SLATE_GRAY;
                        break;
                    case '+':
                        color = SColor.BROWNER;
                        break;
                    case 'A':
                        color = SColor.ALICE_BLUE;
                        break;
                    case 'ø':
                        c = ' ';
                        color = SColor.BLACK;
                        break;
                    default://opaque items
                        color = SColor.DEEP_PINK;
                }
                Item floor = new Item();
                floor.color = color;
                floor.symbol = c;
                tiles[x][y] = floor;
            }
        }

        currentMap = new Map(tiles);
        return currentMap;
    }
}
