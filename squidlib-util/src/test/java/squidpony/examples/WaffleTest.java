package squidpony.examples;

import squidpony.squidgrid.WaffleFill;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 12/24/2016.
 */
public class WaffleTest {
    public static void main(String[] args)
    {
        WaffleFill w = new WaffleFill(4);
        WaffleFill.WaffleTile[] tiles = {
                /*
                w.tile('X', 1,
                        "####",
                        "####",
                        "####",
                        "####"),
                w.tile('X', 8,
                        "....",
                        "....",
                        "....",
                        "...."),
                */
                w.tile('I', 5,
                        "#..#",
                        "#..#",
                        "#..#",
                        "#..#"),
                w.tile('L', 2,
                        "#..#",
                        "#...",
                        "#...",
                        "####"),/*
                w.tile('T', 1,
                        "#..#",
                        "....",
                        "....",
                        "####"),
                w.tile('X', 1,
                        "#..#",
                        "....",
                        "....",
                        "#..#"),*/
        };
        WaffleFill actual = new WaffleFill(tiles, 4, 3, 3, true);
        StatefulRNG rng = new StatefulRNG(0x1337CAFECABAL);
        System.out.println(actual.run(rng, 100));
        DungeonUtility.debugPrint(actual.output());
    }
}
