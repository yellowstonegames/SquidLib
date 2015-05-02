package squidpony.examples;

import squidpony.squidgrid.mapping.styled.DungeonGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

/**
 * Created by Tommy Ettinger on 4/2/2015.
 */
public class StyledDungeonTest {

    public static void main( String[] args )
    {
        DungeonGen bg = new DungeonGen(new RNG(new LightRNG(0x1337deadbeef48aaL)));
        for(TilesetType tt : TilesetType.values())
        {
            System.out.println(tt.toString());
            bg.generate(tt, 80, 40);
            bg.wallWrap();
            System.out.println(bg);
            bg.setDungeon(DungeonUtility.hashesToLines(bg.getDungeon()));
            System.out.println(bg);
            bg.setDungeon(DungeonUtility.doubleWidth(bg.getDungeon()));
            System.out.println(bg);

            System.out.println();
        }
    }
}
