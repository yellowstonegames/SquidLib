package squidpony.squidmath;

import org.junit.Test;
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
            cache.cacheAllPerformance();
            byte[][] gradient = CoordPacker.unpackMultiByte(cache.getCacheEntry(walkable.x, walkable.y), width, height);

            for (int i = 0; i < seen.length; i++) {
                assertArrayEquals(seen[i], gradient[i]);
            }
            cache.cacheAllQuality();
            boolean[][] mutual = CoordPacker.unpack(cache.getCacheEntry(walkable.x, walkable.y)[0], width, height);
            for (int i = 0; i < mutual.length; i++) {
                for (int j = 0; j < mutual[i].length; j++) {
                    if(mutual[i][j] && map[i][j] != '#')
                    {
                        boolean sharing = cache.queryCache(10, i, j, walkable.x, walkable.y);
                        if(!sharing)
                            System.out.println("i: " + i + ", j: " + j + ", x: " + walkable.x + ", y: " + walkable.y);
                        assertTrue(sharing);
                    }
                }
            }


        }
    }
}
