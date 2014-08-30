package squidpony.squidgrid.generation;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import squidpony.squidmath.PerlinNoise;

/**
 * A map generation factory using perlin noise to make island chain style maps.
 *
 * Based largely on work done by Metsa from #rgrd
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class MetsaMapFactory {
//    //HEIGHT LIMITS
//
//    static private double SEALEVEL = 0,
//            BEACHLEVEL = 0.05,
//            PLAINSLEVEL = 0.3,
//            MOUNTAINLEVEL = 0.45,
//            SNOWLEVEL = 0.63,
//            DEEPSEA = -0.1;
////BIOMESTUFF
//    static private final double POLARLIMIT = 0.5,
//            DESERTLIMIT = 0.1;
//
////SHADOW
//    static private final double SHADOWLIMIT = 0.01;
////COLORORDER
///*
//     0 = deepsea
//     1 = beach
//     2 = low
//     3 = high
//     4 = mountain
//     5 = snowcap
//     6 = lowsea
//     */
//    static private final double colors[] = new double[]{0x006994, 0xfee8d6, 0x5b5, 0x171, 0xaaa, 0xeee, 0x0ea1aa};
//    static private final double polarcolors[] = new double[]{0xd4f0ff, 0xe0ffff, 0xeee9e9, 0xeeeaea, 0xaaa, 0xf00, 0xe0ffff};
//    static private final double desertcolors[] = new double[]{0x006994, 0xfee8d6, 0xfee8d6, 0xeed8c6, 0xaaa, 0xeee, 0x0ea1aa};
//
//    static private final int SIZEX = 512 + 256;
//    static private final int SIZEY = 512;
//    static private final int SCALE = 1;
//    static private final int ROADS = 64;
//    static private final int CITYAMOUNT = 64;
//
//    private JFrame frame;
//    private static final int width = SIZEX * SCALE;
//    private static final int height = SIZEY * SCALE;
//    private JPanel context;
//    private double map[][] = new double[SIZEX][SIZEY];
//    private List<Point> cities = new LinkedList<>();
//
//    private int getRandomInt(int min, int max) {
//        return (int) (Math.floor(Math.random() * (max - min)) + min);
//    }
//
//    private String rgba(int r, int g, int b, int a) {
//        return "rgba(" + r + "," + g + "," + b + "," + a + ")";
//    }
//
//    private double getShadow(int x, int y) {
//        if (x == 0 || y == 0) {
//            return 0;
//        }
//        double up = map[x][y - 1];
//        double left = map[x - 1][y];
//        double upleft = map[x - 1][y - 1];
//        double cur = map[x][y];
//        if (cur < 0) {
//            return 0;
//        }
//        //var slope = (cur - up) - (cur - left);
//        double slope = cur - (upleft + up + left) / 3;
//        if (slope < SHADOWLIMIT && slope > -SHADOWLIMIT) {
//            return 0;
//        }
//        if (slope >= SHADOWLIMIT) {
//            return 1; //"alpha"
//        }
////        if (slope <= -SHADOWLIMIT) {
//        return -1;
////        }
//    }
//
//    /**
//     * Finds and returns the closest point containing a city to the given point. Does
//     * not include provided point as a possible city location.
//     * 
//     * If there are no cities, null is returned.
//     * 
//     * @param point
//     * @return 
//     */
//    private Point closestCity(Point point) {
//        double dist = 999999999, newdist;
//        Point  closest = null;
//        for (Point c : cities) {
//            if (c.equals(point)) {
//                continue;//skip the one being tested for
//            }
//            newdist = Math.pow(point.x - c.x, 2) + Math.pow(point.y - c.y, 2);
//            if (newdist < dist) {
//                dist = newdist;
//                closest = c;
//            }
//        }
//        return closest;
//    }
//
//    public static void main(String... args) {
//        new MetsaMapFactory().go();
//    }
//
//    private void go() {
//        double n, dist;
//        map = new double[SIZEX][SIZEY];
//        double highn = 0;
//        int perldivisors[] = new int[]{1, 1, 2, 4, 8, 16, 64};
////Heightmap
//        for (int i = 0; i < SIZEX; i++) {
//            for (int j = 0; j < SIZEY; j++) {
//                //Get noise
//                n = //noise.perlin2(i/256,j/256) / perldivisors[0]
//                        PerlinNoise.noise(i / 128, j / 128) / perldivisors[1]
//                        + PerlinNoise.noise(i / 64, j / 64) / perldivisors[2]
//                        + PerlinNoise.noise(i / 32, j / 32) / perldivisors[3]
//                        + PerlinNoise.noise(i / 16, j / 16) / perldivisors[4]
//                        + PerlinNoise.noise(i / 8, j / 8) / perldivisors[5]
//                        + PerlinNoise.noise(i / 4, j / 4) / perldivisors[6];
//                //+ noise.perlin2(i / 2, j / 2) / 32;
//
//                dist = Math.sqrt(Math.pow(Math.abs(i - SIZEX / 2), 2) + Math.pow(Math.abs(j * (SIZEX / SIZEY) - SIZEX / 2), 2));
//                n -= Math.max(0, Math.pow(dist / (SIZEX / 2), 2) - 0.4);
//
//                //Corresponding tiletypes
//                map[i][j] = n;
//                if (n > highn) {
//                    highn = n;
//                }
//            }
//        }
//
//        System.out.println("highest point is " + highn);
//        SNOWLEVEL = highn - 0.05;
//
////biomes 0 normal 1 snow
//        int biomemap[][] = new int[SIZEX][SIZEY];
//        for (int i = 0;                i < SIZEX;                i++) {
//    for (int j = 0; j < SIZEY; j++) {
//                biomemap[i][j] = 0;
//                double distEq = Math.abs(j - SIZEY / 2) / (SIZEY / 2);
//                distEq = distEq + PerlinNoise.noise(i / 32, j / 32) / 8 + map[i][j] / 32;
//                if (distEq > POLARLIMIT) {
//                    biomemap[i][j] = 1;
//                }
//                if (distEq < DESERTLIMIT) {
//                    biomemap[i][j] = 2;
//                }
//                if (distEq > POLARLIMIT + 0.25) {
//                    biomemap[i][j] = 3;
//                }
//            }
//        }
////NATIONS
///*
//         nationmap, 4 times less accurate map used for nations
//         -1 no nation
//         */
//        int nationmap[][] = new int[SIZEX][SIZEY];
//for (int i = 0;                i < SIZEX / 4; i++) {
//    for (int j = 0; j < SIZEY / 4; j++) {
//                if (map[i * 4][j * 4] < 0) {
//                    nationmap[i][j] = -1;
//                }else{
//                    nationmap[i][j] = 0;
//                }
//            }
//        }
//
////END OF NATIONS :D
////Weighed map for road
//        double weighedMap[][] = new double[SIZEX][SIZEY];
//for (int i = 0;                i < SIZEX                / 4; i++) {
//    for (int j = 0; j < SIZEY / 4; j++) {
//                weighedMap[i][j] = 0;
//                if (map[i * 4][j * 4] > BEACHLEVEL) {
//                    weighedMap[i][j] = 2 + (map[i * 4][j * 4] - PLAINSLEVEL) * 8;
//                }
//                if (map[i][j] <= BEACHLEVEL && map[i * 4][j * 4] >= SEALEVEL) {
//                    weighedMap[i][j] = 2 - (map[i * 4][j * 4]) * 2;
//                }
//            }
//        }
//        int px = 0,                py = 0;
//
//for (int i = 0;                i < CITYAMOUNT;                i++) {
//                px = getRandomInt(0, SIZEX - 1);
//                py = getRandomInt(0, SIZEY - 1);
//                while (map[px][py] < SEALEVEL || map[px][py] > BEACHLEVEL) {
//                    px = getRandomInt(0, SIZEX - 1);
//                    py = getRandomInt(0, SIZEY - 1);
//                }
//                cities.add(new Point(    4 * Math.round(px / 4)   ,    4 * Math.round(py / 4) ));
//              }
//
////Generate a road
//        results = [];
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
////DRAW
//
//        for (i = 0;
//                i < SIZEX;
//                i++) {
//            for (j = 0; j < SIZEY; j++) {
//                //context.fillStyle = colors[map[i][j]];
//                n = map[i][j];
//                var curcolor = colors;
//                if (biomemap[i][j] == 1 || biomemap[i][j] == 3) {
//                    curcolor = polarcolors;
//                }
//                if (biomemap[i][j] == 2) {
//                    curcolor = desertcolors;
//                }
//                context.fillStyle = curcolor[6];
//                if (n > SEALEVEL) {
//                    context.fillStyle = curcolor[1];
//                }
//                if (n > BEACHLEVEL) {
//                    context.fillStyle = curcolor[2];
//                }
//                if (n > PLAINSLEVEL) {
//                    context.fillStyle = curcolor[3];
//                }
//                if (n > MOUNTAINLEVEL) {
//                    context.fillStyle = curcolor[4];
//                }
//                if (n > SNOWLEVEL) {
//                    context.fillStyle = curcolor[5];
//                }
//
//                //Polar ice
//                if (n < DEEPSEA) {
//                    if (biomemap[i][j] == 3) {
//                        context.fillStyle = polarcolors[0];
//                    } else {
//                        context.fillStyle = colors[0];
//                    }
//                }
//                //context.fillStyle = colors[map[i][j]];
//                context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                if (n > 0) {
//                    //Elevation, very slight
//
//                    context.fillStyle = rgba(200, 200, 150, Math.pow(n / highn, 2) / 2);
//                    context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                    context.fillStyle = rgba(25, 25, 100, 0.2 - n * n);
//                    context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                    //SNOWAREA VOLCANO CASE
//                    if (n > SNOWLEVEL && (biomemap[i][j] == 1 || biomemap[i][j] == 3)) {
//                        //shadow side INVERSE
//                        if (getShadow(i, j) == -1) {
//                            context.fillStyle = rgba(0, 0, 90, 0.2);
//                            context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                        }
//                        //sun side INVERSE
//                        if (getShadow(i, j) == 1) {
//                            context.fillStyle = rgba(255, 255, 0, 0.1);
//                            context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                        }
//
//                    } else {
//                        //shadow side
//                        if (getShadow(i, j) == 1) {
//                            context.fillStyle = rgba(0, 0, 90, 0.2);
//                            context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                        }
//                        //sun side
//                        if (getShadow(i, j) == -1) {
//                            context.fillStyle = rgba(220, 220, 100, 0.2);
//                            context.fillRect(i * SCALE, j * SCALE, SCALE, SCALE);
//                        }
//                    }
//
//                } else {
//
//                }
//            }
//
//        }
////draw cities.
//        for (i = 0;
//                i < cities.length;
//                i++) {
//            var city = cities[i];
//            context.fillStyle = "#444";
//            context.fillRect(city.x * SCALE - SCALE * 2, city.y * SCALE - SCALE * 2, SCALE * 4, SCALE * 4);
//        }
//        context.strokeStyle = rgba(0, 0, 0, 0.5);
////draw roads
//        for (i = 0;
//                i < results.length;
//                i++) {
//            var node;
//            var a = results[i].length;
//            context.beginPath();
//            node = results[i][0];
//            context.moveTo(node.x * 4 * SCALE, node.y * 4 * SCALE);
//            for (j = 0; j < a; j++) {
//                node = results[i][j];
//                context.lineTo(node.x * SCALE * 4 + noise.perlin2(node.x / 5, node.y / 5) * 8, node.y * SCALE * 4 + noise.perlin2(node.y / 5, node.x / 5) * 8);
//            }
//            //node = results[i].end;
//            //context.moveTo(node.x*SCALE, node.y*SCALE);
//
//            context.stroke();
//            context.closePath();
//        }
//    }
////Sphere
}
