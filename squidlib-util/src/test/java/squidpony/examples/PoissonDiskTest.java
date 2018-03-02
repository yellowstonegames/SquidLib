package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.SerpentDeepMapGenerator;
import squidpony.squidmath.*;

/**
 * A test for the randomized spraying of Coords in the PoissonDisk class.
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDiskTest {

    public static void main(String[] args) {
        LightRNG lrng = new LightRNG(0xBEEFBABEL);
        RNG rng = new RNG(lrng);
        DungeonGenerator dg = new DungeonGenerator(80, 80, rng);

        SerpentDeepMapGenerator deep = new SerpentDeepMapGenerator(80, 80, 8, rng);
        //deep.putWalledBoxRoomCarvers(1);
        char[][] dun = dg.generateRespectingStairs(deep.generate()[7]);

        // System.out.println(dg);

        OrderedSet<Coord> disks = PoissonDisk.sampleMap(dun, 7f, rng, '#');

        //char[][] hl = DungeonUtility.hashesToLines(dun);
        for(Coord c : disks)
        {
            if(dun[c.x][c.y] != '#')
                dun[c.x][c.y] = 'o';
        }
        //hl[entry.x][entry.y] = '@';
        dg.setDungeon(dun);
        System.out.println(dg);

        System.out.println();
    }

}
