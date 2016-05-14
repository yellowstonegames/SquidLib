package squidpony.examples;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.OrganicMapGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by Tommy Ettinger on 5/13/2016.
 */
public class FOVTest {
    public static int width = 100, height = 100, depth = 16;

    public static void main(String[] args) {
        StatefulRNG rng = new StatefulRNG(0xCAFEBA77L);
        DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(15);
        dungeonGenerator.addGrass(5);
        dungeonGenerator.addBoulders(5);
        OrganicMapGenerator organic = new OrganicMapGenerator(width, height, rng);
        dungeonGenerator.generate(organic.generate());
        char[][] dungeon = dungeonGenerator.getDungeon();
        char[][] deco = DungeonUtility.doubleWidth(DungeonUtility.hashesToLines(dungeon, true));
        //char[][] bare = dungeonGenerator.getBareDungeon();
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';

        double[][] resMap = DungeonUtility.generateResistances(dungeon);
        FOV fov = new FOV(FOV.SHADOW);
        ArrayList<double[][]> fovMaps = new ArrayList<>(8);
        short[] floors = DungeonUtility.packedFloors(dungeon);
        Coord pt = dungeonGenerator.utility.randomCell(floors);
        Coord start = pt;
        LinkedHashSet<Coord> points = new LinkedHashSet<>(20);
        double[][] losMap = fov.calculateLOSMap(resMap, pt.x, pt.y);
        for (int i = 0; i < 20; i++) {
            points.add(pt);
            fovMaps.add(fov.calculateFOV(resMap, pt.x, pt.y, rng.between(4.0, 9.0), Radius.CIRCLE));
            pt = dungeonGenerator.utility.randomCell(floors);
        }
        double[][] result = FOV.mixVisibleFOVs(losMap, fovMaps);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(dungeon[x][y] == '#')
                {
                    System.out.print(deco[x * 2][y]);
                    System.out.print(deco[x * 2 + 1][y]);
                }
                else if (start.x == x && start.y == y)
                {
                    System.out.print("!!");
                }
                else if(points.contains(Coord.get(x, y)))
                {
                    System.out.print("**");
                }
                else
                {
                    System.out.print(' ');
                    System.out.print(Math.round(result[x][y] * 9.4999));
                }
            }
            System.out.println();
        }
        System.out.println("\n");

        result = FOV.mixVisibleFOVs(losMap, fovMaps.toArray(new double[fovMaps.size()][][]));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(dungeon[x][y] == '#')
                {
                    System.out.print(deco[x * 2][y]);
                    System.out.print(deco[x * 2 + 1][y]);
                }
                else if (start.x == x && start.y == y)
                {
                    System.out.print("!!");
                }
                else if(points.contains(Coord.get(x, y)))
                {
                    System.out.print("**");
                }
                else
                {
                    System.out.print(' ');
                    System.out.print(Math.round(result[x][y] * 9.4999));
                }
            }
            System.out.println();
        }

        System.out.println("\n");

        result = FOV.addFOVs(fovMaps);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(dungeon[x][y] == '#')
                {
                    System.out.print(deco[x * 2][y]);
                    System.out.print(deco[x * 2 + 1][y]);
                }
                else if (start.x == x && start.y == y)
                {
                    System.out.print("!!");
                }
                else if(points.contains(Coord.get(x, y)))
                {
                    System.out.print("**");
                }
                else
                {
                    System.out.print(' ');
                    System.out.print(Math.round(result[x][y] * 9.4999));
                }
            }
            System.out.println();
        }


        System.out.println("\n");

        result = FOV.addFOVs(fovMaps.toArray(new double[fovMaps.size()][][]));
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if(dungeon[x][y] == '#')
                {
                    System.out.print(deco[x * 2][y]);
                    System.out.print(deco[x * 2 + 1][y]);
                }
                else if (start.x == x && start.y == y)
                {
                    System.out.print("!!");
                }
                else if(points.contains(Coord.get(x, y)))
                {
                    System.out.print("**");
                }
                else
                {
                    System.out.print(' ');
                    System.out.print(Math.round(result[x][y] * 9.4999));
                }
            }
            System.out.println();
        }

        System.out.println();
    }
}
