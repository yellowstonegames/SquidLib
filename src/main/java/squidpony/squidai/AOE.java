package squidpony.squidai;

import java.awt.*;
import java.util.HashMap;

/**
 * Area of Effect data.
 * Created by Tommy Ettinger on 5/8/2015.
 */
public class AOE {
    /**
     * The pattern of cells that will all be affected by this AOE. Point 0,0 is the center of the effect. The CellRepeat
     * values corresponding to a Point determine if and how more cells should be affected. A CellRepeat with kind=SINGLE
     * will only correspond to that single Point key, or rotations as well if rotate=true. If a CellRepeat has
     * rotate=true, then the four cardinal rotations of the Point key around 0,0 as the origin will also be included
     * when determining AOE. Bursts or explosions typically only need one Point key with a CellRepeat value that has
     * kind=EXPAND_MANHATTAN or kind=EXPAND_CHEBYSHEV, as appropriate, and a length set to the radius. Straight line
     * effects should have kind=LINE, a length corresponding to how long the line should travel, and a Point key that
     * is adjacent to the origin (The move from origin to that Point will be repeated a number of times equal to length,
     * so a straight line going north, or a different cardinal direction if the AOE has been rotated, will have a key
     * of 0,1. A straight diagonal line could use key 1,1. If rotate=true for one of these CellRepeat-s, all four
     * rotations will have a line travel outward.) Where kind=LINE will always travel in one direction, using the same
     * Point to determine each of its steps, kind=FREE will use the entire pattern at each step, up to a maximum number
     * of steps specified by length (kind=FREE should probably not be mixed with other kinds in one pattern).
     */
    public HashMap<Point, CellRepeat> pattern = new HashMap<Point, CellRepeat>();

    /**
     * Constructs an AOE with an empty pattern. Positions and CellRepeats can be added to the pattern using the standard
     * HashMap methods.
     */
    public AOE()
    {
    }

    /**
     * Constructs an AOE with a given pattern.
     * @param pattern The pattern to copy.
     */
    public AOE(HashMap<Point, CellRepeat> pattern)
    {
        this.pattern = pattern;
    }

    /**
     * Constructs an AOE with a series of Point positions specified by cells, and each one of those positions given
     * a CellRepeat that does not rotate and affects only that exact Point (specificalt, kind=SINGLE, length=1).
     * @param cells The vararg array of Points to use in this AOE.
     */
    public AOE(Point ... cells)
    {
        for(Point p : cells)
        {
            this.pattern.put(p, CellRepeat.DEFAULT_AOE);
        }
    }

}
