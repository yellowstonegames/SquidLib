package squidpony.squidgrid.mapping;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import squidpony.SColor;
import squidpony.SColorFactory;
import squidpony.annotation.Beta;
import squidpony.squidgrid.gui.SquidPanel;
import squidpony.squidmath.PerlinNoise;
import squidpony.squidmath.RNG;

/**
 * A map generation factory using perlin noise to make island chain style maps.
 *
 * Based largely on work done by Metsa from #rgrd
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class MetsaMapFactory {
    //HEIGHT LIMITS

    static private double SEALEVEL = 0,
            BEACHLEVEL = 0.05,
            PLAINSLEVEL = 0.3,
            MOUNTAINLEVEL = 0.45,
            SNOWLEVEL = 0.63,
            DEEPSEA = -0.1;
//BIOMESTUFF
    static private final double POLARLIMIT = 0.5, DESERTLIMIT = 0.1;
    static private final SColor CITY_COLOR = new SColor(0x444);

//SHADOW
    static private final double SHADOWLIMIT = 0.01;
//COLORORDER
/*
     0 = deepsea
     1 = beach
     2 = low
     3 = high
     4 = mountain
     5 = snowcap
     6 = lowsea
     */
    static private final SColor[] colors = new SColor[]{SColor.DENIM, SColor.TAN, SColor.ASPARAGUS,
        SColor.FOREST_GREEN, SColor.SLATE_GRAY, SColor.ALICE_BLUE, SColor.AZUL};
    static private final SColor[] polarcolors = colors;
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};
    static private final SColor[] desertcolors = colors;
//            new SColor[]{SColor.DARK_SLATE_GRAY, SColor.SCHOOL_BUS_YELLOW, SColor.YELLOW_GREEN,
//        SColor.GREEN_BAMBOO, SColorFactory.lighter(SColor.LIGHT_BLUE_SILK), SColor.ALICE_BLUE, SColor.AZUL};

    static private final int width = 1500;
    static private final int height = 1000;
    static private final int scale = 1;
    static private final int ROADS = 64;
    static private final int CITYAMOUNT = 14;

    private JFrame frame;
    private SquidPanel back, front;
    private List<Point> cities = new LinkedList<>();
    private final RNG rng = new RNG();
    private double highn = 0;

    public static void main(String... args) {
        new MetsaMapFactory().go();
    }

    private void go() {
        back = new SquidPanel(width, height, scale, scale);
        front = new SquidPanel(width, height, scale, scale);
        double[][] map = makeHeightMap();
        int[][] biomeMap = makeBiomeMap(map);
        int[][] nationMap = makeNationMap(map);
        double[][] weighedMap = makeWeighedMap(map);
//        generateRoads();

        paint(map, biomeMap);
    }

    private int getShadow(int x, int y, double[][] map) {
        if (x >= width - 1 || y <= 0) {
            return 0;
        }
        double upRight = map[x + 1][y - 1];
        double right = map[x + 1][y];
        double up = map[x][y - 1];
        double cur = map[x][y];
        if (cur <= 0) {
            return 0;
        }
        double slope = cur - (upRight + up + right) / 3;
        if (slope < SHADOWLIMIT && slope > -SHADOWLIMIT) {
            return 0;
        }
        if (slope >= SHADOWLIMIT) {
            return -1; //"alpha"
        }
        if (slope <= -SHADOWLIMIT) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Finds and returns the closest point containing a city to the given point.
     * Does not include provided point as a possible city location.
     *
     * If there are no cities, null is returned.
     *
     * @param point
     * @return
     */
    private Point closestCity(Point point) {
        double dist = 999999999, newdist;
        Point closest = null;
        for (Point c : cities) {
            if (c.equals(point)) {
                continue;//skip the one being tested for
            }
            newdist = Math.pow(point.x - c.x, 2) + Math.pow(point.y - c.y, 2);
            if (newdist < dist) {
                dist = newdist;
                closest = c;
            }
        }
        return closest;
    }

    private double[][] makeHeightMap() {
        double[][] map = MapFactory.heightMap(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double n = map[x][y];
                if (n > highn) {
                    highn = n;
                }
            }
        }
        SNOWLEVEL = highn - 0.05;

        return map;
    }

    private int[][] makeBiomeMap(double[][] map) {
        //biomes 0 normal 1 snow
        int biomeMap[][] = new int[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                biomeMap[x][y] = 0;
                double distanceFromEquator = Math.abs(y - height / 2) / (height / 2);
                distanceFromEquator += PerlinNoise.noise(x / 32, y / 32) / 8 + map[x][y] / 32;
                if (distanceFromEquator > POLARLIMIT) {
                    biomeMap[x][y] = 1;
                }
                if (distanceFromEquator < DESERTLIMIT) {
                    biomeMap[x][y] = 2;
                }
                if (distanceFromEquator > POLARLIMIT + 0.25) {
                    biomeMap[x][y] = 3;
                }
            }
        }
        return biomeMap;
    }

    private int[][] makeNationMap(double[][] map) {
        // nationmap, 4 times less accurate map used for nations -1 no nation
        int nationMap[][] = new int[width][height];
        for (int i = 0; i < width / 4; i++) {
            for (int j = 0; j < height / 4; j++) {
                if (map[i * 4][j * 4] < 0) {
                    nationMap[i][j] = -1;
                } else {
                    nationMap[i][j] = 0;
                }
            }
        }
        return nationMap;
    }

    private double[][] makeWeighedMap(double[][] map) {
        //Weighed map for road
        double weighedMap[][] = new double[width][height];
        for (int i = 0; i < width / 4; i++) {
            for (int j = 0; j < height / 4; j++) {
                weighedMap[i][j] = 0;
                if (map[i * 4][j * 4] > BEACHLEVEL) {
                    weighedMap[i][j] = 2 + (map[i * 4][j * 4] - PLAINSLEVEL) * 8;
                }
                if (map[i][j] <= BEACHLEVEL && map[i * 4][j * 4] >= SEALEVEL) {
                    weighedMap[i][j] = 2 - (map[i * 4][j * 4]) * 2;
                }
            }
        }

        for (int i = 0; i < CITYAMOUNT; i++) {
            int px = rng.between(0, width);
            int py = rng.between(0, height);
            while (map[px][py] < SEALEVEL || map[px][py] > BEACHLEVEL) {
                px = rng.between(0, width);
                py = rng.between(0, height);
            }
            cities.add(new Point(4 * Math.round(px / 4), 4 * Math.round(py / 4)));
        }
        return weighedMap;
    }

    private void generateRoads() {
        ////Generate a road
//Queue results
//if (ROADS > 0) {
//            var graph = new Graph(weighedMap, {
//                diagonal
//        
//            : true
//    });
//    console.log(cities.length);
//            for (ii = 0; ii < cities.length; ii++) {
//                var startc = cities[ii];
//                console.log(start);
//                var start = graph.grid[startc.x / 4][startc.y / 4];
//                var endc = closestCity(cities[ii]);
//                console.log(end);
//                var end = graph.grid[endc.x / 4][endc.y / 4];
//
//                var res = astar.search(graph, start, end);
//                if (res.length != 0) {
//                    results.push(res);
//                }
//            }
//
//        }
    }

    private void paint(double[][] map, int[][] biomeMap) {
        //DRAW
        double n;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                n = map[x][y];
                SColor[] curcolor = colors;
                if (biomeMap[x][y] == 1 || biomeMap[x][y] == 3) {
                    curcolor = polarcolors;
                }
                if (biomeMap[x][y] == 2) {
                    curcolor = desertcolors;
                }
                SColor color = curcolor[6];
                if (n > SEALEVEL) {
                    color = curcolor[1];
                }
                if (n > BEACHLEVEL) {
                    color = curcolor[2];
                }
                if (n > PLAINSLEVEL) {
                    color = curcolor[3];
                }
                if (n > MOUNTAINLEVEL) {
                    color = curcolor[4];
                }
                if (n > SNOWLEVEL) {
                    color = curcolor[5];
                }

                //Polar ice
                if (n < DEEPSEA) {
                    if (biomeMap[x][y] == 3) {
                        color = polarcolors[0];
                    } else {
                        color = colors[0];
                    }
                }

//                use alpha to blend
                if (n > 0) {
                    color = SColorFactory.blend(color, SColor.ALICE_BLUE, Math.pow(n / highn, 2) / 2);//hight stuff gets lighter
                    color = SColorFactory.blend(color, SColor.DARK_BLUE_DYE, 0.2 - n * n);//low stuff gets darker

                    int shadow = getShadow(x, y, map);
                    if (n > SNOWLEVEL && (biomeMap[x][y] == 1 || biomeMap[x][y] == 3)) {//SNOWAREA VOLCANO CASE
                        if (shadow == -1) {//shadow side INVERSE
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = SColorFactory.blend(color, SColor.DENIM, 0.2 * n / 2);
                        }
                        if (shadow == 1) {//sun side INVERSE
//                            color = SColorFactory.blend(color, new SColor(255, 255, 0), 0.1);
                            color = SColorFactory.blend(color, SColor.BRASS, 0.1 * n / 2);
                        }
                    } else {
                        if (shadow == 1) { //shadow side
//                            color = SColorFactory.blend(color, new SColor(0, 0, 90), 0.2);
                            color = SColorFactory.blend(color, SColor.ONANDO, 0.2 * n / 2);
                        }
                        if (shadow == -1) {//sun side
//                            color = SColorFactory.blend(color, new SColor(220, 220, 100), 0.2);
                            color = SColorFactory.blend(color, SColor.YELLOW, 0.2 * n / 2);
                        }
                    }
                }
                back.put(x, y, color);
            }
        }

        for (Point city : cities) {
            front.put(city.x, city.y, 'C', CITY_COLOR);
        }

        back.refresh();
        front.refresh();

        frame = new JFrame();
        JLayeredPane layer = new JLayeredPane();
        layer.setLayer(back, JLayeredPane.DEFAULT_LAYER);
        layer.setLayer(front, JLayeredPane.PALETTE_LAYER);
        layer.add(back);
        layer.add(front);
        layer.setPreferredSize(back.getPreferredSize());
        layer.setSize(back.getPreferredSize());
        frame.add(layer);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
