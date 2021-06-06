package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SerpentDeepMapGenerator;
import squidpony.squidmath.*;

/**
 * A test for the randomized spraying of Coords in the PoissonDisk class.
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDiskTest {

    public static void main(String[] args) {
        RNG rng = new RNG(0xBEEFBABEL);
        DungeonGenerator dg = new DungeonGenerator(80, 80, rng);
        char[][] dun = dg.generate();

        // System.out.println(dg);

        OrderedSet<Coord> disks = PoissonDisk.sampleMap(dun, 4f, rng, '#');

        //char[][] hl = DungeonUtility.hashesToLines(dun);
        for (Coord c : disks) {
            if (dun[c.x][c.y] != '#')
                dun[c.x][c.y] = 'o';
        }
        //hl[entry.x][entry.y] = '@';
        dg.setDungeon(dun);
        if (TestConfiguration.PRINTING) {
            System.out.println(dg);

            System.out.println();
        }
        ArrayTools.fill(dun, '.');
        DungeonUtility.wallWrap(dun);

        OrderedSet<Coord> points = PoissonDisk.sampleRectangle(
                Coord.get(1, 1), Coord.get(78, 78), 2.5f,
                80, 80, 30, rng);
        for (int i = 0; i < points.size(); i++) {
            Coord c = points.getAt(i);
            dun[c.x][c.y] = '*';
        }
        dg.setDungeon(dun);
        if (TestConfiguration.PRINTING) {

            System.out.println(dg);
            System.out.println();
        }
    }
}
