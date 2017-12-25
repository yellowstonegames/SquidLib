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
        for(int l : new int[]{1, 3, 4, 5, 6, 7, 8, 9}) {
            //seed is, in base 36, the number SQUIDLIB
            StatefulRNG rng = new StatefulRNG(2252637788195L);
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

            char[][] dungeon = dungeonGenerator.generate(TilesetType.OPEN_AREAS);
            char[][] bare = dungeonGenerator.getBareDungeon();
            GreasedRegion floors = new GreasedRegion(bare, '.').retract();
            Coord start = floors.singleRandom(rng);
            GreasedRegion flooded = new GreasedRegion(start, width, height).flood(floors.expand(), 16),
                    outside = flooded.copy().expand8way(1), temp = new GreasedRegion(width, height);
            ArrayList<Coord> allSeen = new ArrayList<>(128), targets = new ArrayList<>(5);
            LOS los;
            if(l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.SQUARE);
            rng.nextLong();

            for (int i = 0; i < 4; i++) {
                Coord end = temp.empty().insert(start).flood8way(floors, 3).notAnd(flooded).singleRandom(rng);

                targets.add(end);
                if(l < 7)
                    los.isReachable(bare, start.x, start.y, end.x, end.y);
                else
                    los.spreadReachable(bare, start.x, start.y, end.x, end.y, Radius.CIRCLE, l - 7);
                allSeen.addAll(los.getLastPath());
            }
            dungeon = outside.mask(dungeon, ' ');
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
        for(int l : new int[]{1, 3, 4, 5, 6, 7, 8, 9}) {
            //seed is, in base 36, the number SQUIDLIB
            StatefulRNG rng = new StatefulRNG(2252637788195L);
            DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

            char[][] dungeon = dungeonGenerator.generate(TilesetType.OPEN_AREAS);
            char[][] bare = dungeonGenerator.getBareDungeon();
            GreasedRegion floors = new GreasedRegion(bare, '.').retract();
            Coord start = floors.singleRandom(rng);
            GreasedRegion flooded = new GreasedRegion(start, width, height).flood(floors.expand(), 16),
                    outside = flooded.copy().expand8way(1), temp = new GreasedRegion(width, height);
            ArrayList<Coord> allSeen = new ArrayList<>(128), targets = new ArrayList<>(5);
            LOS los;
            if(l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.CIRCLE);
            rng.nextLong();

            for (int i = 0; i < 4; i++) {
                Coord end = temp.empty().insert(start).flood8way(floors, 3).notAnd(flooded).singleRandom(rng);

                targets.add(end);
                if(l < 7)
                    los.isReachable(bare, start.x, start.y, end.x, end.y);
                else
                    los.spreadReachable(bare, start.x, start.y, end.x, end.y, Radius.CIRCLE, l - 7);
                allSeen.addAll(los.getLastPath());
            }
            dungeon = outside.mask(dungeon, ' ');
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
