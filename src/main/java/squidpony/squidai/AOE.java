package squidpony.squidai;

import java.awt.*;
import java.util.HashMap;

/**
 * Area of Effect interface meant to be implemented by various specific burst, line, flowing, and user-made AOE types.
 * Created by Tommy Ettinger on 5/8/2015.
 */
public interface AOE {
    /**
     * This must be called before findArea() can be called, and takes a char[][] with '#' for walls, '.' for floors.
     * It must be bounded with walls, which DungeonGenerator does automatically.
     * @param map width first, height second, 2D char array.
     */
    void setMap(char[][] map);
    /**
     * This is how an AOE interacts with anything that uses it. It expects a map to have already been set with setMap,
     * with '#' for walls, '.' for floors and potentially other chars that implementors can use if they are present in
     * the map. The map must be bounded by walls, which DungeonGenerator does automatically and other generators can
     * easily add with two loops.
     *
     * This returns a HashMap of Point keys to Double values; if a cell is 100% affected by the AOE then the value
     * should be 1.0; if it is 50% affected it should be 0.5, if unaffected should be 0.0, etc. The Point keys should
     * have the same x and y as the x,y map positions they correspond to.
     * @return a HashMap of Point keys to Double values from 1.0 (fully affected) to 0.0 (unaffected).
     */
    HashMap<Point, Double> findArea();
}
