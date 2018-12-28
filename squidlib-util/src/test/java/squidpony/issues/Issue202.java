package squidpony.issues;

import org.junit.Test;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.StatefulRNG;

/**
 * Check for <a href="https://github.com/SquidPony/SquidLib/issues/202">SquidLib Issue 202</a>.
 * <br>
 * Created by Tommy Ettinger using nateroe's sample code on 12/27/2018.
 */
public class Issue202 {
    
    @Test
    public void testIssue()
    {
        for (int i = 0; i < 100; i++) {
            DungeonUtility.debugPrint(reproduce());
        }
    }
    
    public char[][] reproduce()
    {
        StatefulRNG rng = new StatefulRNG();
        int width = rng.between(10, 25);
        int height = rng.between(10, 25);
        DungeonGenerator generator = new DungeonGenerator(width, height, rng);
        generator.addDoors(45, true);
        return generator.generate();
    }
}
