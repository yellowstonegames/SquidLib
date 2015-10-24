package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.PoissonDisk;
import squidpony.squidmath.RNG;

import java.util.ArrayList;

/**
 * A test for the randomized spraying of Coords in the PoissonDisk class.
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDiskTest {

    public static void main(String[] args) {
        LightRNG lrng = new LightRNG(0xbeefbabel);
        RNG rng = new RNG(lrng);
        DungeonGenerator dg = new DungeonGenerator(80, 40, rng);

        char[][] dun = dg.generate();

        // System.out.println(dg);

        ArrayList<Coord> disks = PoissonDisk.sampleRectangle(Coord.get(1,1), Coord.get(78,38), 6, 80, 40, 5, rng);

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
