package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.List;

/**
 * A quick test to visually compare the results of five different LOS algorithms.
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class LOSComparisonTest {
    public static int width = 35, height = 22;
    public static void main( String[] args )
    {
        for(int l : new int[]{1, 3, 4, 5, 6, 7, 8, 9}) {
            //seed is, in base 36, the number SQUIDLIB
            StatefulRNG rng = new StatefulRNG(new LightRNG(2252637788195L));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

            char[][] dungeon = dungeonGenerator.generate(TilesetType.ROUND_ROOMS_DIAGONAL_CORRIDORS);
            char[][] bare = dungeonGenerator.getBareDungeon();
            short[] floors = CoordPacker.pack(bare, '.');
            Coord start = dungeonGenerator.utility.randomCell(floors);
            short[] flooded = CoordPacker.flood(floors, CoordPacker.packOne(start), 10, true);
            short[] outside = CoordPacker.differencePacked(CoordPacker.rectangle(width, height),// flooded);
                    CoordPacker.expand(flooded, 1, width, height));
            List<Coord> allSeen = new ArrayList<>(23 * 23), targets = new ArrayList<>(5);
            LOS los;
            if(l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.SQUARE);
            for (int i = 0; i < 4; i++) {
                rng.nextLong();
                Coord end = CoordPacker.singleRandom(flooded, rng);
                targets.add(end);
                if(l < 7)
                    los.isReachable(bare, start.x, start.y, end.x, end.y);
                else
                    los.spreadReachable(bare, start.x, start.y, end.x, end.y, Radius.CIRCLE, l - 7);
                allSeen.addAll(los.getLastPath());
            }
            for(Coord c : CoordPacker.allPacked(outside))
            {
                dungeon[c.x][c.y] = ' ';
            }
            for(Coord c : allSeen)
            {
                dungeon[c.x][c.y] = '*';
            }
            for(Coord c : targets)
            {
                dungeon[c.x][c.y] = '$';
            }
            dungeon[start.x][start.y] = '@';
            dungeonGenerator.setDungeon(dungeon);
            System.out.println(dungeonGenerator);
        }
    }

}
