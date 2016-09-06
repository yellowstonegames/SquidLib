package squidpony.examples;

import regexodus.Category;
import squidpony.GwtCompatibility;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.*;

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
            StatefulRNG rng = new StatefulRNG(0x1337deadbeefc000L);
            DungeonGenerator dg = new DungeonGenerator(40, 40, rng);

            char[][] dun = dg.generate();
            Spill spreader = new Spill(dun, m);

            System.out.println(dg);

            Coord entry = dg.utility.randomFloor(dun);
            HashSet<Coord> impassable = new HashSet<>();
            impassable.add(Coord.get(entry.x + 2, entry.y));
            impassable.add(Coord.get(entry.x - 2, entry.y));
            impassable.add(Coord.get(entry.x, entry.y + 2));
            impassable.add(Coord.get(entry.x, entry.y - 2));
            ArrayList<Coord> ordered = spreader.start(entry, 20, impassable);
            ordered.addAll(spreader.start(entry, 35, impassable));
            boolean[][] sm = spreader.spillMap;
            char[][] md = GwtCompatibility.copy2D(dun),
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
            StatefulRNG rng = new StatefulRNG(0x1337deadbeefc000L);
            DungeonGenerator dg = new DungeonGenerator(80, 80, rng);
            char[][] dun = dg.generate();
            MultiSpill spreader = new MultiSpill(dun, m, rng);

            System.out.println(dg);
            GreasedRegion valid = new GreasedRegion(dun, '.');
            OrderedMap<Coord, Double> entries = new OrderedMap<>(16);
            Coord[] section = valid.randomSeparated(20.0 / valid.count(), rng);
            for (int i = 0; i < 16 && i < section.length; i++) {
                entries.put(section[i], 1.0 - (i & 3) * 0.25);
            }

            ArrayList<ArrayList<Coord>> ordered = spreader.start(entries, -1, null);
            short[][] sm = spreader.spillMap;
            char[][] md = GwtCompatibility.copy2D(dun),
                    hl = DungeonUtility.hashesToLines(dun);
            for (int x = 0; x < md.length; x++) {
                for (int y = 0; y < md[x].length; y++) {
                    if (sm[x][y] >= 0)
                        md[x][y] = (char)('A' + sm[x][y]);
                    else
                        md[x][y] = hl[x][y];
                }
            }
            for(Coord c : entries.keySet())
            {
                md[c.x][c.y] = '@';
            }
            dg.setDungeon(md);
            System.out.println(dg);

            System.out.println();
        }
        char[] glyphs = Category.Ll.contents();
        //int gs = glyphs.length;
        for (int i = 0; i < 5; i++) {
            StatefulRNG rng = new StatefulRNG(i * 65567 + 257);
            int dim = 40 + i * 40, count = 30 + 20 * i;
            char[][] blank = GwtCompatibility.fill2D('~', dim, dim);
            MultiSpill spreader = new MultiSpill(blank, Spill.Measurement.MANHATTAN, rng);

            //SobolQRNG sobol = new SobolQRNG(3);
            //double[] filler = sobol.skipTo(rng.between(1000, 6500));
            OrderedMap<Coord, Double> entries = new OrderedMap<>(count);
            for (int j = 0; j < count; j++) {
                //sobol.fillVector(filler);
                //entries.put(Coord.get((int)(dim * filler[0]), (int)(dim * filler[1])), (filler[2] + 0.25) / 1.25);
                entries.put(rng.nextCoord(dim, dim), (rng.nextDouble() + 0.25) / 1.25);
            }
            ArrayList<ArrayList<Coord>> ordered = spreader.start(entries, -1, null);
            short[][] sm = spreader.spillMap;
            for (int x = 0; x < dim; x++) {
                for (int y = 0; y < dim; y++) {
                    //blank[x][y] = (char) ('a' + Integer.bitCount(sm[x][y] + 7) % 26);
                    if((sm[x][y] & 1) == 0)
                        blank[x][y] = glyphs[sm[x][y]];
                }
            }
            for(Coord c : entries.keySet())
            {
                if((sm[c.x][c.y] & 1) == 0)
                    blank[c.x][c.y] = '@';
            }
            DungeonUtility.debugPrint(blank);
            System.out.println();
        }
    }
}
