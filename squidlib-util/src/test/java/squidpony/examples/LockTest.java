package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.MixedGenerator;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.locks.constraints.CountConstraints;
import squidpony.squidgrid.mapping.locks.constraints.ILayoutConstraints;
import squidpony.squidgrid.mapping.locks.generators.LayoutGenerator;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 1/4/2017.
 */
public class LockTest {
    public static final int width = 140, height = 80;
    public static void main(String[] args)
    {
        StatefulRNG rng = new StatefulRNG(0x1337BEEFBABBL);
        ILayoutConstraints constraints = new CountConstraints(20, 5, 0);
        LayoutGenerator gen = new LayoutGenerator(rng, constraints);
        MixedGenerator mix;
        SectionDungeonGenerator sdg = new SectionDungeonGenerator(width, height, rng);
        sdg.addDoors(20, false);
        try{
            gen.generate();
            mix = new MixedGenerator(width, height, rng, gen.getRoomLayout(), 0.75f);
        } catch (GenerationFailureException e)
        {
            mix = new MixedGenerator(width, height, rng);
        }
        mix.putBoxRoomCarvers(3);
        mix.putRoundRoomCarvers(1);
        char[][] dungeon = mix.generate();
        DungeonUtility.debugPrint(sdg.generate(dungeon, mix.getEnvironment()));
    }
}
