package squidpony.examples;

import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.MixedGenerator;
import squidpony.squidgrid.mapping.SectionDungeonGenerator;
import squidpony.squidgrid.mapping.locks.Edge;
import squidpony.squidgrid.mapping.locks.IRoomLayout;
import squidpony.squidgrid.mapping.locks.Room;
import squidpony.squidgrid.mapping.locks.Symbol;
import squidpony.squidgrid.mapping.locks.constraints.CountConstraints;
import squidpony.squidgrid.mapping.locks.constraints.ILayoutConstraints;
import squidpony.squidgrid.mapping.locks.generators.LayoutGenerator;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.ShortSet;
import squidpony.squidmath.StatefulRNG;

/**
 * Created by Tommy Ettinger on 1/4/2017.
 */
public class LockTest {
    public static final int width = 140, height = 80;
    public static void main(String[] args)
    {
        StatefulRNG rng = new StatefulRNG(0x1337BEEFBABBL);
        ILayoutConstraints constraints = new CountConstraints(50, 5, 0);
        LayoutGenerator gen = new LayoutGenerator(rng, constraints);
        MixedGenerator mix;
        SectionDungeonGenerator sdg = new SectionDungeonGenerator(width, height, rng);
        sdg.addDoors(90, false);
        char[][] dungeon;
        try{
            gen.generate();
            IRoomLayout layout = gen.getRoomLayout();
            mix = new MixedGenerator(width, height, rng, layout, 0.75f);
            mix.putBoxRoomCarvers(3);
            mix.putRoundRoomCarvers(1);
            dungeon = mix.generate();
            Coord cen;
            ShortSet links = new ShortSet(layout.roomCount() + 16, 0.4f);
            int id, id2;
            short pair;
            for(Room room : layout.getRooms())
            {
                if(!Symbol.isNothing(room.getItem()))
                {
                    cen = room.getCenter();
                    dungeon[cen.x][cen.y] = Symbol.asChar(room.getItem());
                }
                id = room.id;
                for(Edge edge : room.getEdges())
                {
                    if(edge.hasSymbol())
                    {
                        id2 = edge.getTargetRoomId();
                        if(id < id2)
                            pair = (short) ((id2 & 0xff)<<8|(id & 0xff));
                        else
                            pair = (short) ((id & 0xff)<<8|(id2 & 0xff));
                        if(!links.contains(pair))
                        {
                            links.add(pair);
                            cen = layout.get(id2).getCenter().average(room.getCenter());
                            dungeon[cen.x][cen.y] = Symbol.asChar(edge.getSymbol());
                        }
                    }
                }

            }
        } catch (GenerationFailureException e)
        {
            mix = new MixedGenerator(width, height, rng);
            mix.putBoxRoomCarvers(3);
            mix.putRoundRoomCarvers(1);
            dungeon = mix.generate();

        }
        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(sdg.generate(dungeon, mix.getEnvironment())));
    }
}
