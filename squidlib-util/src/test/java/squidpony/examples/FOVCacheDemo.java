package squidpony.examples;

import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 10/8/2015.
 */
public class FOVCacheDemo {
    public static void main(String[] args)
    {
        int width = 60;
        int height = 60;
        for (long r = 0, seed = 0xD00D; r < 10; r++, seed ^= seed << 2) {


            StatefulRNG rng = new StatefulRNG(new LightRNG(seed));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);
            dungeonGenerator.addDoors(25, true);
            dungeonGenerator.addWater(25);
            //dungeonGenerator.addTraps(2);
            char[][] map = DungeonUtility.closeDoors(dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON));
            /*
            LinkedHashMap<Coord, Integer> lights = new LinkedHashMap<Coord, Integer>((width / 6) * (height / 6));

            for (int i = 0; i < width / 6; i++) {
                for (int j = 0; j < height / 4; j++) {
                    lights.put(Coord.get(i * 6 + 3, j * 4 + i % 4), 2);
                    map[i * 6 + 3][j * 4 + i % 4] = (map[i * 6 + 3][j * 4 + i % 4] == '#') ? '┼' : '^';
                }
            }
            */
            FOVCache cache = new FOVCache(map, 10, Radius.CIRCLE, 8);
            Coord walkable = dungeonGenerator.utility.randomFloor(map);
            long time = System.currentTimeMillis();
            cache.awaitCache();
            time = System.currentTimeMillis() - time;
            System.out.println("Time spent caching: " + time);
            //byte[][] gradient = CoordPacker.unpackMultiByte(cache.getCacheEntry(walkable.x, walkable.y), width, height);
            double[][] gradient = cache.calculateGradedFOV(null, walkable.x, walkable.y, 10);
            //double[][] conical = cache.calculateGradedFOV(new double[1][1], walkable.x, walkable.y, 12, Radius.CIRCLE, rng.nextDouble(360.0), rng.nextDouble(75) + 75);
            for (int j = 0; j < map[0].length; j++) {
                for (int i = 0; i < map.length; i++) {
                    System.out.print(map[i][j]);
                    if (gradient[i][j] > 0)
                    {
                        System.out.print((char) ((int) Math.round(gradient[i][j] * 10) + 65));
                        if(map[i][j] == '+')
                            map[i][j] = '/';
                    }
                    else
                        System.out.print(' ');
                }
                System.out.println();
            }
            System.out.println();


            time = System.currentTimeMillis();
            cache.awaitRefresh(map);
            time = System.currentTimeMillis() - time;
            System.out.println("Time spent refreshing: " + time);
            //byte[][] gradient = CoordPacker.unpackMultiByte(cache.getCacheEntry(walkable.x, walkable.y), width, height);
            gradient = cache.calculateGradedFOV(null, walkable.x, walkable.y, 10);
            //double[][] conical = cache.calculateGradedFOV(new double[1][1], walkable.x, walkable.y, 12, Radius.CIRCLE, rng.nextDouble(360.0), rng.nextDouble(75) + 75);
            for (int j = 0; j < map[0].length; j++) {
                for (int i = 0; i < map.length; i++) {
                    System.out.print(map[i][j]);
                    if (gradient[i][j] > 0)
                        System.out.print((char) ((int)Math.round(gradient[i][j] * 10) + 65));
                    else
                        System.out.print(' ');
                }
                System.out.println();
            }
            System.out.println();

        }
/*        for (int n = 1; n < 8; n++) {
            System.out.println("With viewer at " + walkable.x + "," + walkable.y + " and target at " +
                    (walkable.x - 1) + "," + (walkable.y + n) + ": Can they see each other? " +
                    cache.isCellVisible(16, walkable.x, walkable.y, walkable.x - 1, walkable.y + n));
        }*/
    }
}
