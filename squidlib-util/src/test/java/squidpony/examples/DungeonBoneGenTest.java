package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 9/13/2019.
 */
public class DungeonBoneGenTest {
    public static void main(String[] args)
    {
        DungeonGenerator dungeonGenerator;
        char[][] dungeon;

        dungeonGenerator= new DungeonGenerator(100, 340, new StatefulRNG(2252637788195L));
        dungeon = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';
        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeon, true));
        System.out.println(dungeonGenerator);
        System.out.println();

        dungeonGenerator= new DungeonGenerator(60, 60, new StatefulRNG(2252637788195L));
        dungeon = dungeonGenerator.generate(TilesetType.DEFAULT_DUNGEON);
        dungeon[dungeonGenerator.stairsUp.x][dungeonGenerator.stairsUp.y] = '<';
        dungeon[dungeonGenerator.stairsDown.x][dungeonGenerator.stairsDown.y] = '>';
        dungeonGenerator.setDungeon(
                DungeonUtility.hashesToLines(dungeon, true));
        System.out.println(dungeonGenerator);
        System.out.println();

    }
}
