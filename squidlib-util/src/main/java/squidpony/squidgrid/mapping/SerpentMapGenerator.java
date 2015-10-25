package squidpony.squidgrid.mapping;

import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.RNG;

import java.util.ArrayList;
import java.util.List;

/**
 * Generate dungeons based on a random, winding, looping path through 2D space. Uses techniques from MixedGenerator.
 * Uses a Moore Curve, which is related to Hilbert Curves but loops back to its starting point, and stretches and
 * distorts the grid to make sure a visual correlation isn't obvious.
 * <br>
 * The name comes from a vivid dream I had about gigantic, multi-colored snakes that completely occupied a roguelike
 * dungeon. Shortly after, I made the connection to the Australian mythology I'd heard about the Rainbow Serpent, which
 * in some stories dug water-holes and was similarly gigantic.
 * Created by Tommy Ettinger on 10/24/2015.
 */
public class SerpentMapGenerator {
    private MixedGenerator mix;
    private int width, height;
    private int[] columns, rows;
    private RNG random;

    public SerpentMapGenerator(int width, int height, RNG rng)
    {
        if(width <= 2 || height <= 2)
            throw new ExceptionInInitializerError("width and height must be greater than 2");
        random = rng;
        long columnAlterations = random.nextLong(0x10000);
        float columnBase = width / (Long.bitCount(columnAlterations) + 48.0f);
        long rowAlterations = random.nextLong(0x10000);
        float rowBase = height / (Long.bitCount(rowAlterations) + 48.0f);

        columns = new int[16];
        rows = new int[16];
        int csum = 0, rsum = 0;
        for (int i = 0, b = 7; i < 16; i++, b <<= 3) {
            columns[i] = csum + (int)(columnBase * 0.5f * (3 + Long.bitCount(columnAlterations & b)));
            csum += (int)(columnBase * (3 + Long.bitCount(columnAlterations & b)));
            rows[i] = rsum + (int)(rowBase * 0.5f * (3 + Long.bitCount(rowAlterations & b)));
            rsum += (int)(rowBase * (3 + Long.bitCount(rowAlterations & b)));
        }
        int cs2 = (int)Math.floor((width - csum) * 0.5);
        int rs2 = (int)Math.floor((height - rsum) * 0.5);
        int cs3 = (width == csum) ? 0 :  (int)Math.ceil((width - csum) * 0.5);
        int rs3 = (height == rsum) ? 0 : (int)Math.ceil((height - rsum) * 0.5);
        columns[7] += cs2;
        rows[7] += rs2;
        columns[8] += cs3;
        rows[8] += rs3;

        List<Coord> points = new ArrayList<Coord>(80);
        Coord temp;
        for (int i = 0, m = random.nextInt(256), r; i < 256; r = random.between(4, 12), i += r, m += r) {
            temp = CoordPacker.mooreToCoord(m);
            points.add(Coord.get(columns[temp.x], rows[temp.y]));
        }
        points.add(points.get(0));
        mix = new MixedGenerator(width, height, random, points);

    }

    public void putCaveCarvers(int count)
    {
        mix.putCaveCarvers(count);
    }
    public void putBoxRoomCarvers(int count)
    {
        mix.putBoxRoomCarvers(count);
    }
    public void putRoundRoomCarvers(int count)
    {
        mix.putRoundRoomCarvers(count);
    }
    public char[][] generate()
    {
        return mix.generate();
    }
}
