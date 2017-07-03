package squidpony.squidmath;

import squidpony.ArrayTools;

/**
 * Created by Tommy Ettinger on 7/3/2017.
 */
public class CellularAutomaton {
    /**
     * Returned directly by some methods, but you may want to change this at some other point.
     */
    public GreasedRegion current;
    private GreasedRegion[] neighbors = new GreasedRegion[9];
    private int[][] sums;

    /**
     * Constructs a CellularAutomaton with an unfilled 64x64 GreasedRegion, that can be altered later via {@link #current}.
     */
    public CellularAutomaton()
    {
        this(new GreasedRegion(64, 64));
    }

    /**
     * Constructs a CellularAutomaton with an unfilled GreasedRegion of the specified width and height, that can be
     * altered later via {@link #current}.
     * @param width the width of the CellularAutomaton
     * @param height the height of the CellularAutomaton
     */
    public CellularAutomaton(int width, int height)
    {
        this(new GreasedRegion(Math.max(1, width), Math.max(1, height)));
    }

    /**
     * Stores a direct reference to {@code current} as this object's {@link #current} field, and initializes the other
     * necessary fields.
     * @param current a GreasedRegion that will be used directly; changes will be shared
     */
    public CellularAutomaton(GreasedRegion current) {
        this.current = current;
        for (int i = 0; i < 9; i++) {
            neighbors[i] = current.copy();
        }
        sums = new int[current.width][current.height];
    }

    /**
     * Reduces the sharpness of corners by only considering a cell on if the previous version has 5 of the 9 cells in
     * the containing 3x3 area as "on." Typically, this method is run repeatedly. It does not return itself for
     * chaining, because it returns a direct reference to the {@link #current} GreasedRegion that this will use for
     * any future calls to this, and changes to current will be used here.
     * @return a direct reference to the changed GreasedRegion this considers its main state, {@link #current}
     */
    public GreasedRegion runBasicSmoothing()
    {
        neighbors[0].remake(current).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
        neighbors[8].remake(current);
        ArrayTools.fill(sums, 0);
        GreasedRegion.sumInto(sums, neighbors);
        return current.refill(sums, 0, 5);
    }

    /**
     * Runs one step of the simulation called Conway's Game of Life, which has relatively simple rules:
     * <ul>
     *     <li>Any "on" cell with fewer than two "on" neighbors becomes "off."</li>
     *     <li>Any "on" cell with two or three "on" neighbors (no more than three) stays "on."</li>
     *     <li>Any "on" cell with more than three "on" neighbors becomes "off."</li>
     *     <li>Any "off" cell with exactly three "on" neighbors becomes "on."</li>
     * </ul>
     * These rules can bring about complex multi-step patterns in many cases, eventually stabilizing to predictable
     * patterns in most cases. Filling the whole state of this CellularAutomaton won't produce interesting patterns
     * most of the time, even if the fill is randomized; you might have better results by using known patterns. Some
     * key well-known patterns are covered on <a href="https://en.wikipedia.org/wiki/Conway's_Game_of_Life">Wikipedia's
     * detailed article on Conway's Game of Life</a>.
     * @return a direct reference to the changed GreasedRegion this considers its main state, {@link #current}
     */
    public GreasedRegion runGameOfLife()
    {
        neighbors[0].remake(current).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
        neighbors[8].remake(current);
        ArrayTools.fill(sums, 0);
        GreasedRegion.sumInto(sums, neighbors);
        return current.refill(sums,3).or(neighbors[0].refill(sums, 4).and(neighbors[8]));
    }
}
