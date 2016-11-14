package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.*;

import java.util.ArrayList;

/**
 * A quick test to visually compare the results of five different LOS algorithms.
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class LOSComparisonTest {
    public static int width = 37, height = 23;
    public static void main( String[] args )
    {
        for(int l : new int[]{1, 3, 4, 5, 6, 7, 8, 9, 3}) {
            //seed is, in base 36, the number SQUIDLIB
            StatefulRNG rng = new StatefulRNG(new ThunderRNG(2252637788195L));
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

            char[][] dungeon = dungeonGenerator.generate(TilesetType.OPEN_AREAS);
            char[][] bare = dungeonGenerator.getBareDungeon();
            short[] floors = CoordPacker.pack(bare, '.');
            Coord start = dungeonGenerator.utility.randomCell(floors);
            short[] flooded = CoordPacker.flood(floors, CoordPacker.packOne(start), 11, true);
            short[] outside = CoordPacker.differencePacked(CoordPacker.rectangle(width, height),// flooded);
                    CoordPacker.expand(flooded, 1, width, height));
            ArrayList<Coord> allSeen = new ArrayList<>(128), targets = new ArrayList<>(5);
            LOS los;
            if(l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.SQUARE);
            rng.nextLong();
            CoordPacker.singleRandom(
                    CoordPacker.differencePacked(flooded,
                            CoordPacker.flood(floors, CoordPacker.packOne(start), 3, true)),
                    rng);
            for (int i = 0; i < 2; i++) {
                Coord end = CoordPacker.singleRandom(
                        CoordPacker.differencePacked(flooded,
                                CoordPacker.flood(floors, CoordPacker.packOne(start), 3, true)),
                        rng);

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
