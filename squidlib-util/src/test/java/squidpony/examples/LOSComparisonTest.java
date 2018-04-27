package squidpony.examples;

import squidpony.squidgrid.LOS;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * A quick test to visually compare the results of five different LOS algorithms.
 * Created by Tommy Ettinger on 4/8/2015.
 * @author Tommy Ettinger - https://github.com/tommyettinger
 */
public class LOSComparisonTest {
    public static int width = 37, height = 23;

    public static void main(String[] args) {
        //seed is, in base 36, the number SQUIDLIB
        StatefulRNG rng = new StatefulRNG(2252637788195L);
        DungeonGenerator dungeonGenerator = new DungeonGenerator(width, height, rng);

        char[][] baseDungeon = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        char[][] bare = dungeonGenerator.getBareDungeon();
        GreasedRegion floors = new GreasedRegion(bare, '.').retract();
        Coord start = floors.singleRandom(rng);
        GreasedRegion flooded = new GreasedRegion(start, width, height).flood(floors.expand(), 16),
                outside = flooded.copy().expand8way(1), temp = new GreasedRegion(width, height);
        char[][] dungeon;
        ArrayList<Coord> targets = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            Coord end = temp.empty().insert(start).flood8way(floors, 3).notAnd(flooded).singleRandom(rng);
            flooded.remove(end);
            targets.add(end);
        }
        for (int l : new int[]{LOS.BRESENHAM, LOS.ELIAS, LOS.RAY, LOS.ORTHO, LOS.DDA, LOS.THICK, 7, 8, 9}) {
            ArrayList<Coord> allSeen = new ArrayList<>(128);
            LOS los;
            if (l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.SQUARE);
            for (Coord end : targets) {
                if (l < 7) {
                    if (los.isReachable(bare, start.x, start.y, end.x, end.y))
                        allSeen.addAll(los.getLastPath());
                } else {
                    if (los.spreadReachable(bare, start.x, start.y, end.x, end.y, Radius.CIRCLE, l - 7))
                        allSeen.addAll(los.getLastPath());
                }
            }
            dungeon = outside.mask(baseDungeon, ' ');
            for (Coord c : allSeen) {
                if(dungeon[c.x][c.y] == '.')
                    dungeon[c.x][c.y] = '*'; // on the way to a visible target
            }
            for (Coord c : targets) {
                if(dungeon[c.x][c.y] == '*')
                    dungeon[c.x][c.y] = '$'; // reached
                else 
                    dungeon[c.x][c.y] = '?'; // not reached
            }
            dungeon[start.x][start.y] = '@'; // start cell
            dungeonGenerator.setDungeon(dungeon);
            System.out.println(dungeonGenerator);
        }
        for (int l : new int[]{LOS.BRESENHAM, LOS.ELIAS, LOS.RAY, LOS.ORTHO, LOS.DDA, LOS.THICK, 7, 8, 9}) {
            ArrayList<Coord> allSeen = new ArrayList<>(128);
            LOS los;
            if (l < 7) los = new LOS(l);
            else los = new LOS(6);
            los.setRadiusStrategy(Radius.CIRCLE); // different from above loop here

            for (Coord end : targets) {
                if (l < 7) {
                    if (los.isReachable(bare, start.x, start.y, end.x, end.y))
                        allSeen.addAll(los.getLastPath());
                } else {
                    if (los.spreadReachable(bare, start.x, start.y, end.x, end.y, Radius.CIRCLE, l - 7))
                        allSeen.addAll(los.getLastPath());
                }
            }
            dungeon = outside.mask(baseDungeon, ' ');
            for (Coord c : allSeen) {
                if(dungeon[c.x][c.y] == '.')
                    dungeon[c.x][c.y] = '*'; // on the way to a visible target
            }
            for (Coord c : targets) {
                if(dungeon[c.x][c.y] == '*')
                    dungeon[c.x][c.y] = '$'; // reached
                else
                    dungeon[c.x][c.y] = '?'; // not reached
            }
            dungeon[start.x][start.y] = '@'; // start cell
            dungeonGenerator.setDungeon(dungeon);
            System.out.println(dungeonGenerator);
        }
    }
}