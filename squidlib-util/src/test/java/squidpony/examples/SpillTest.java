package squidpony.examples;

import squidpony.ArrayTools;
import squidpony.FakeLanguageGen;
import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.SpillWorldMap;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * A test for the randomized flood-fill in the Spill class. This runs the Spill twice from the same starting position,
 * which turns out to yield better results with Chebyshev and Euclidian measurements.
 * Created by Tommy Ettinger on 4/7/2015.
 */
public class SpillTest {

    public static void main(String[] args) {
        for (int which = 0; which < 2; which++) {
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
                char[][] md = ArrayTools.copy(dun),
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
                Coord[] section;
                if(which == 0)
                    section = valid.mixedRandomSeparated(20.0 / valid.size(), -1, rng.nextLong());
                else
                    section = valid.quasiRandomSeparated(20.0 / valid.size());
                for (int i = 0; i < 16 && i < section.length; i++) {
                    entries.put(section[i], 1.0 - (i & 3) * 0.25);
                }

                ArrayList<ArrayList<Coord>> ordered = spreader.start(entries, -1, null);
                short[][] sm = spreader.spillMap;
                char[][] md = ArrayTools.copy(dun),
                        hl = DungeonUtility.hashesToLines(dun);
                for (int x = 0; x < md.length; x++) {
                    for (int y = 0; y < md[x].length; y++) {
                        if (sm[x][y] >= 0)
                            md[x][y] = (char) ('A' + sm[x][y]);
                        else
                            md[x][y] = hl[x][y];
                    }
                }
                for (Coord c : entries.keySet()) {
                    md[c.x][c.y] = '@';
                }
                dg.setDungeon(md);
                System.out.println(dg);

                System.out.println();
            }
            char[] glyphs = "adeghjklnprstwxy".toCharArray(); //Category.IdentifierStart.contents();
            //int gs = glyphs.length;
            for (int i = 3; i < 3; i++) {
                StatefulRNG rng = new StatefulRNG(); //i * 5617
                int dim = 40 + i * 40, count = 20 + 10 * i * i;
                char[][] blank = ArrayTools.fill('~', dim, dim);
                MultiSpill spreader = new MultiSpill(blank, Spill.Measurement.MANHATTAN, rng);

                //SobolQRNG sobol = new SobolQRNG(3);
                //double[] filler = sobol.skipTo(rng.between(1000, 6500));
                OrderedMap<Coord, Double> entries = new OrderedMap<>(count);
                for (int j = 0; j < count; j++) {
                    //sobol.fillVector(filler);
                    //entries.put(Coord.get((int)(dim * filler[0]), (int)(dim * filler[1])), (filler[2] + 0.25) / 1.25);
                    entries.put(rng.nextCoord(dim - 14, dim - 14).add(7), (rng.nextDouble() + 0.25) * 0.8);
                }
                count = entries.size();
                int extra = (dim - 2) * 2;
                double edgePower = count * 0.6 / extra;
                for (int x = 1; x < dim - 1; x += 2) {
                    entries.put(Coord.get(x, 0), edgePower);
                    entries.put(Coord.get(x, dim - 1), edgePower);
                }
                for (int y = 1; y < dim - 1; y += 2) {
                    entries.put(Coord.get(0, y), edgePower);
                    entries.put(Coord.get(dim - 1, y), edgePower);
                }
                ArrayList<ArrayList<Coord>> ordered = spreader.start(entries, -1, null);
                short[][] sm = spreader.spillMap;
                for (int x = 0; x < dim; x++) {
                    for (int y = 0; y < dim; y++) {
                        //blank[x][y] = (char) ('a' + Integer.bitCount(sm[x][y] + 7) % 26);
                        if (sm[x][y] < count && (sm[x][y] & 15) <= 10)
                            blank[x][y] = glyphs[sm[x][y] & 15];
                    }
                }
                for (Coord c : entries.keySet()) {
                    if (sm[c.x][c.y] < count && (sm[c.x][c.y] & 15) <= 10)
                        blank[c.x][c.y] = '@';
                }
                DungeonUtility.debugPrint(blank);
                System.out.println();
            }
            int factions = 40;
            int w = 100, h = 60;
            SpillWorldMap swm;
            StatefulRNG stable = new StatefulRNG(0x123456789ABCDEF0L);
            for (String world : new String[]{
                    FakeLanguageGen.NORSE.addModifiers(FakeLanguageGen.Modifier.SIMPLIFY_NORSE).word(stable, true),
                    FakeLanguageGen.JAPANESE_ROMANIZED.word(stable, true),
                    FakeLanguageGen.SWAHILI.word(stable, true),
                    FakeLanguageGen.RUSSIAN_ROMANIZED.word(stable, true),
                    FakeLanguageGen.NAHUATL.word(stable, true),
            }) {
                swm = new SpillWorldMap(w += 20, h += 15, world);
                System.out.println(world + '_' + w + 'x' + h);
                DungeonUtility.debugPrint(swm.generate(factions, true, stable.between(0.7, 1.0), w * 0.03));
                System.out.println("     Atlas for the world of " + world);
                for (int i = 0; i < factions + 2; i++) {
                    System.out.println("  " + swm.atlas.keyAt(i) + "  :  " + swm.atlas.getAt(i));
                }
                System.out.println();
            }
        }
    }
}
