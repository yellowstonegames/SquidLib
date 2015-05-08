package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.styled.DungeonBoneGen;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;
import squidpony.squidgrid.Spill;

import java.awt.Point;
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
            LightRNG lrng = new LightRNG(0x1337deadbeefc000l);
            RNG rng = new RNG(lrng);
            DungeonUtility.rng = rng;
            DungeonBoneGen dg = new DungeonBoneGen(rng);

            dg.generate(TilesetType.DEFAULT_DUNGEON, 40, 40);
            dg.wallWrap();

            char[][] dun = dg.getDungeon();
            Spill spreader = new Spill(dun, m);

            System.out.println(dg);

            Point entry = DungeonUtility.randomFloor(dun);
            HashSet<Point> impassable = new HashSet<Point>();
            impassable.add(new Point(entry.x + 2, entry.y));
            impassable.add(new Point(entry.x - 2, entry.y));
            impassable.add(new Point(entry.x, entry.y + 2));
            impassable.add(new Point(entry.x, entry.y - 2));
            ArrayList<Point> ordered = spreader.start(entry, 20, impassable);
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
    }
}
