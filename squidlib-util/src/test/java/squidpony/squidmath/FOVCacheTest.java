package squidpony.squidmath;

import org.junit.Test;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;

/**
 * Created by Tommy Ettinger on 10/8/2015.
 */
public class FOVCacheTest {
    public static void main(String[] args)
    {
        int width = 60;
        int height = 60;
        for (long r = 0, seed = 0xD00D; r < 8; r++, seed ^= seed << 2) {


            StatefulRNG rng = new StatefulRNG(new LightRNG(seed));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);
            dungeonGenerator.addDoors(15, true);
            dungeonGenerator.addWater(25);
            //dungeonGenerator.addTraps(2);
            char[][] map = DungeonUtility.closeDoors(dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON));

            FOV fov = new FOV();
            FOVCache cache = new FOVCache(fov, map, 20, Radius.CIRCLE, 8);
            Coord walkable = dungeonGenerator.utility.randomFloor(map);

            cache.cacheAll();
            byte[][] gradient = CoordPacker.unpackMultiByte(cache.getCacheEntry(walkable.x, walkable.y), width, height);
            for (int j = 0; j < map[0].length; j++) {
                for (int i = 0; i < map.length; i++) {
                    if (gradient[i][j] > 0)
                        System.out.print((char) (gradient[i][j] + 65));
                    else
                        System.out.print(' ');
                    System.out.print(map[i][j]);
                }
                System.out.println();
            }
        }
/*        for (int n = 1; n < 8; n++) {
            System.out.println("With viewer at " + walkable.x + "," + walkable.y + " and target at " +
                    (walkable.x - 1) + "," + (walkable.y + n) + ": Can they see each other? " +
                    cache.isCellVisible(16, walkable.x, walkable.y, walkable.x - 1, walkable.y + n));
        }*/
    }
}
