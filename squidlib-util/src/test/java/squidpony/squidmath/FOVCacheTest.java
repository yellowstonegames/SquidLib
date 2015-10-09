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
    @Test
    public void testCacheAll()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(60, 60, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = DungeonUtility.closeDoors(dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON));

        FOV fov = new FOV();
        FOVCache cache = new FOVCache(fov, map, 16, Radius.CIRCLE, 4);
        /*Coord walkable = dungeonGenerator.utility.randomFloor(map);
        byte[][] gradient = cache.waveFOV(walkable.x, walkable.y);
        for (int j = 0; j < map[0].length; j++) {
            for (int i = 0; i < map.length; i++) {
                if(gradient[i][j] > 0)
                    System.out.print(gradient[i][j]);
                else
                    System.out.print(' ');
                System.out.print(map[i][j]);
            }
            System.out.println();
        }*/
        cache.cacheAll();
    }
}
