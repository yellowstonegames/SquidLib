package squidpony.squidmath;

import org.junit.Test;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.FOVCache;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;

import static org.junit.Assert.*;

/**
 * Created by Tommy Ettinger on 10/8/2015.
 */
public class FOVCacheTest {
    @Test
    public void testCache()
    {
        int width = 60;
        int height = 60;
        for (long r = 0, seed = 0xCAB; r < 10; r++, seed ^= seed << 2) {
            StatefulRNG rng = new StatefulRNG(new LightRNG(seed));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);
            dungeonGenerator.addDoors(15, true);
            dungeonGenerator.addWater(25);
            //dungeonGenerator.addTraps(2);
            char[][] map = DungeonUtility.closeDoors(dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON));

            FOVCache cache = new FOVCache(map, 10, Radius.CIRCLE, 8);
            Coord walkable = dungeonGenerator.utility.randomFloor(map);
            byte[][] seen = cache.waveFOV(walkable.x, walkable.y);
            cache.cacheAll();
            byte[][] gradient = CoordPacker.unpackMultiByte(cache.getCacheEntry(walkable.x, walkable.y), width, height);

            for (int i = 0; i < seen.length; i++) {
                assertArrayEquals(seen[i], gradient[i]);
            }

        }
    }
}
