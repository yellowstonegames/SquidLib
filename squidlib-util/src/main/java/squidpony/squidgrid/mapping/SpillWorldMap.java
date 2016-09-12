package squidpony.squidgrid.mapping;

import squidpony.squidgrid.MultiSpill;
import squidpony.squidgrid.Spill;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.StatefulRNG;

import java.util.ArrayList;

/**
 * Created by Tommy Ettinger on 9/12/2016.
 */
public class SpillWorldMap {

    public int width;
    public int height;
    public StatefulRNG rng;
    public SpillWorldMap(int width, int height, long seed)
    {
        this.width = Math.max(width, 20);
        this.height = Math.max(width, 20);
        rng = new StatefulRNG(seed);
    }

    public char[][] generate(char... factions) {
        if(factions == null || factions.length == 0)
            return new char[width][height];
        int count = 10 + 10 * (width >>> 4) * (height >>> 4), factionCount = factions.length;
        MultiSpill spreader = new MultiSpill(new short[width][height], Spill.Measurement.MANHATTAN, rng);

        //SobolQRNG sobol = new SobolQRNG(3);
        //double[] filler = sobol.skipTo(rng.between(1000, 6500));
        OrderedMap<Coord, Double> entries = new OrderedMap<>(count);
        for (int j = 0; j < count; j++) {
            //sobol.fillVector(filler);
            //entries.put(Coord.get((int)(dim * filler[0]), (int)(dim * filler[1])), (filler[2] + 0.25) / 1.25);
            entries.put(rng.nextCoord(width - 14, height - 14).add(7), (rng.nextDouble() + 0.25) * 0.8);
        }
        count = entries.size();
        int extra = (width - 1) + (height - 1);
        double edgePower = count * 0.6 / extra;
        for (int x = 1; x < width - 1; x += 2) {
            entries.put(Coord.get(x, 0), edgePower);
            entries.put(Coord.get(x, height - 1), edgePower);
        }
        for (int y = 1; y < height - 1; y += 2) {
            entries.put(Coord.get(0, y), edgePower);
            entries.put(Coord.get(width - 1, y), edgePower);
        }
        ArrayList<ArrayList<Coord>> ordered = spreader.start(entries, -1, null);
        short[][] sm = spreader.spillMap;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //blank[x][y] = (char) ('a' + Integer.bitCount(sm[x][y] + 7) % 26);
                if (sm[x][y] >= count || (sm[x][y] & 15) > 10)
                    sm[x][y] = -1;
                else
                    sm[x][y] = 0;
            }
        }
        GreasedRegion map = new GreasedRegion(sm, 0, 0x7fff);
        Coord[] centers = map.randomPortion(rng, factionCount);
        int volume = (int) (map.count() * rng.between(0.75, 1.0));

        spreader.initialize(sm);
        entries.clear();
        entries.put(Coord.get(-1,-1), 0.0);
        for (int i = 0; i < factionCount; i++) {
            entries.put(centers[i], rng.between(0.4, 1.0));
        }
        spreader.start(entries, volume, null);
        sm = spreader.spillMap;
        char[][] world = new char[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = (sm[x][y] == -1) ? '~' : (sm[x][y] == 0) ? '%' : factions[sm[x][y] - 1];
            }
        }
        return world;
    }
}
