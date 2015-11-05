package squidpony.examples;

import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A test for the randomized flood-fill in the Spill class. This runs the Spill twice from the same starting position,
 * which turns out to yield better results with Chebyshev and Euclidian measurements.
 * Created by Tommy Ettinger on 4/7/2015.
 */
public class SpillTest {

    public static void main(String[] args) {
        for (Spill.Measurement m : Spill.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x1337deadbeefc000L);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);

            char[][] dun = dg.generate();
            Spill spreader = new Spill(dun, m);

            System.out.println(dg);

            Coord entry = dg.utility.randomFloor(dun);
            HashSet<Coord> impassable = new HashSet<Coord>();
            impassable.add(Coord.get(entry.x + 2, entry.y));
            impassable.add(Coord.get(entry.x - 2, entry.y));
            impassable.add(Coord.get(entry.x, entry.y + 2));
            impassable.add(Coord.get(entry.x, entry.y - 2));
            ArrayList<Coord> ordered = spreader.start(entry, 20, impassable);
            ordered.addAll(spreader.start(entry, 35, impassable));
            boolean[][] sm = spreader.spillMap;
            char[][] md = dun.clone(),
                    hl = DungeonUtility.hashesToLines(dun);
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    char t;
                    if (sm[x][y])
                        t = '~';
                    else
                        t = hl[x][y];
                    md[x][y] = t;
                }
            }
            md[entry.x][entry.y] = '@';
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();
        }
        for (Spill.Measurement m : Spill.Measurement.values()) {
            LightRNG lrng = new LightRNG(0x1337deadbeefc000L);
            RNG rng = new RNG(lrng);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);
            char[][] dun = dg.generate();
            MultiSpill spreader = new MultiSpill(dun, m, rng);

            System.out.println(dg);
            short[] valid = CoordPacker.pack(dun, '.');

            ArrayList<Coord> entry = CoordPacker.randomPortion(valid, 16, rng);

            ArrayList<ArrayList<Coord>> ordered = spreader.start(entry, -1, null);
            short[][] sm = spreader.spillMap;
            char[][] md = dun.clone(),
                    hl = DungeonUtility.hashesToLines(dun);
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    if (sm[x][y] >= 0)
                        md[x][y] = (char)('A' + sm[x][y]);
                    else
                        md[x][y] = hl[x][y];
                }
            }
            for(Coord c : entry)
            {
                md[c.x][c.y] = '@';
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();
        }
    }
}
