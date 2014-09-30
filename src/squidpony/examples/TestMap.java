package squidpony.examples;

import squidpony.SColor;

/**
 * This class holds the items needed for constructing and using a test map for various grid algorithms.
 *
 * @author @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class TestMap {

    private static final String[] DEFAULT_MAP = new String[]{
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
        "#..................................#.....#...#..................#........./...uu...um≈≈mu.....m≈≈≈mø",
        "#..................................#.....#...#..................#...#+###+#..uuuuuuuu≈≈uu.u.ummmmuuø",
        "#..................................#.....#...#.................##...#..#c.#uuuuuuuuuu≈≈uuuAuuuuuuuuø",
        "#..................................#.....#...#................#.#...#E.#t.#uuuuuAuuA≈≈≈≈≈uuuuuuuuuuø",
        "#..................................#.....#...#...............#..#...#E<#c.#uuAuAuuu≈≈≈≈≈≈≈AuAAuuAuuø",
        "#..................................#.....#...#.............##.../...#######uAuAAA≈≈≈≈≈≈≈≈≈≈AAAAAAAuø",
        "#..................................#.....#...#............#.....#...#.....#AAAuA≈≈≈≈≈≈≈≈≈≈≈AAAAAAAAø",
        "#..................................#.....#...#............#.....#...#.....#AAAA≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAAAø",
        "#..................................#.....#...####################...#.....#AAAAu≈≈≈≈≈≈≈≈≈≈≈≈≈≈AAAAAø",
        "#............................#.....#.....#.......EEEEEEEEEEE........#.....#AAAAuu.≈≈≈≈mmm≈≈≈≈≈AAuAAø",
        "#..................................#.....#........####.####.........#.....#AAAuuuu≈≈≈≈≈mm≈≈≈≈AAuuAAø",
        "#..................................#.....#..........................#.....#AAAAuuuu≈≈≈≈≈≈≈≈≈AAuuuAAø",
        "#..................................#.....####+#.....##..........###/#.....#AAAAAAAuu..≈≈≈≈≈AAAAuAAAø",
        "#..................................#.....#E.+.#.....##..........#.........#AAAAAAAAA.AAA≈≈AAAAAAAAAø",
        "#............................##....#.....####.#.....##..........#tttt+#...#AAAAAAAA..AAAuuu.uAAAAAAø",
        "#...................#..............#.....#E.+.#.....#...........#..c..#...#AAAAAAA....AAAAu..uAAAAAø",
        "#...................#..............#.....####.#.....#...........###..E#...#AAAAAA...AAAAAuuu.uAAAAAø",
        "#..................................#.....#E.+.#.....#...........#E+.EE#...#AAAAAAAAAAAAAAAAAuuAAAAAø",
        "###########################################################################AAAAAAAAAAAAAAAAAAAAAAAAø"
    };

    private int width = DEFAULT_MAP[0].length(), height = DEFAULT_MAP.length;
    private DemoCell[][] map;
    private double[][] resistances;

    public TestMap() {
        map = new DemoCell[width][height];
        resistances = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                char c = DEFAULT_MAP[y].charAt(x);
                map[x][y] = buildCell(c);
                resistances[x][y] = map[x][y].resistance;
            }
        }
    }
    
    public double[][] resistances(){
        return resistances;
    }
    
    public char symbol(int x, int y){
        return map[x][y].representation;
    }
    
    public SColor color(int x, int y){
        return map[x][y].color;
    }
    
    public int width(){
        return width;
    }
    
    public int height(){
        return height;
    }

    /**
     * Builds a cell based on the character in the map.
     *
     * @param c
     * @return
     */
    private DemoCell buildCell(char c) {
        float resistance = 0f;//default is transparent
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
                resistance = 0.3f;
                break;
            case 'm':
                color = SColor.BAIKO_BROWN;
                resistance = 0.1f;
                break;
            case 'u':
                color = SColor.TAN;
                resistance = 0.2f;
                break;
            case 'T':
            case '₤':
                color = SColor.FOREST_GREEN;
                resistance = 0.7f;
                break;
            case 'E':
                color = SColor.SILVER;
                resistance = 0.8f;
                break;
            case 'S':
                color = SColor.BREWED_MUSTARD_BROWN;
                resistance = 0.9f;
                break;
            case '#':
                color = SColor.SLATE_GRAY;
                resistance = 1f;
                break;
            case '+':
                color = SColor.BROWNER;
                resistance = 1f;
                break;
            case 'A':
                color = SColor.ALICE_BLUE;
                resistance = 1f;
                break;
            case 'ø':
                c = ' ';
                color = SColor.BLACK;
                resistance = 1f;
                break;
            default://opaque items
                resistance = 1f;//unknown is opaque
                color = SColor.DEEP_PINK;
        }
        return new DemoCell(resistance, c, color);
    }

    private class DemoCell {

        float resistance;
        char representation;
        SColor color;

        /**
         * Creates a new cell which has minimal properties needed to represent it.
         *
         * @param resistance
         * @param light
         * @param representation
         */
        public DemoCell(float resistance, char representation, SColor color) {
            this.resistance = resistance;
            this.representation = representation;
            this.color = color;
        }
    }
}
