package squidpony.squidmath;

import org.junit.Test;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;

/**
 * Created by Tommy Ettinger on 10/8/2015.
 */
public class FOVCacheTest {
    @Test
    public void testCacheAll()
    {
        StatefulRNG rng = new StatefulRNG(new LightRNG(0xAAAA2D2));
        DungeonGenerator dungeonGenerator = new DungeonGenerator(240, 240, rng);
        dungeonGenerator.addDoors(15, true);
        dungeonGenerator.addWater(25);
        dungeonGenerator.addTraps(2);
        char[][] map = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);

        FOV fov = new FOV();
        FOVCache cache = new FOVCache(fov, map, 10, 4);
        cache.cacheAll();
    }
}
