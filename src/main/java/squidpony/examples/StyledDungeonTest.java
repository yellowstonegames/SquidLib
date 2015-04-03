package squidpony.examples;

import squidpony.squidgrid.mapping.styled.DungeonGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;

/**
 * Created by Tommy Ettinger on 4/2/2015.
 */
public class StyledDungeonTest {

    public static void main( String[] args )
    {
        DungeonGen bg = new DungeonGen(new LightRNG(0x1337deadbeef4efaL));
        for(TilesetType tt : TilesetType.values())
        {
            System.out.println(tt.toString());
            bg.generate(tt, 80, 80);
            bg.wallWrap();
            System.out.println(bg);
            bg.setDungeon(DungeonUtility.hashesToLines(bg.getDungeon()));
            System.out.println(bg);

            System.out.println();
        }
    }
}
