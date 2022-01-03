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
     * Re-initializes this CellularAutomaton using a different GreasedRegion as a basis. If the previous GreasedRegion
     * used has the same dimensions as {@code next}, then this performs no allocations and simply sets the existing
     * contents. Otherwise, it makes one new 2D array and also has all 9 of the internal GreasedRegions adjust in size,
     * which involves some allocation. If {@code next} is null, this does nothing and returns itself without changes.
     * @param next a GreasedRegion to set this CellularAutomaton to read from and adjust
     * @return this, for chaining
     */
    public CellularAutomaton remake(GreasedRegion next)
    {
        if(next == null)
            return this;
        if(current.width != next.width || current.height != next.height)
            sums = new int[next.width][next.height];
        else
            ArrayTools.fill(sums, 0);
        current = next;
        for (int i = 0; i < 9; i++) {
            neighbors[i].remake(current);
        }
        return this;
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
        return current.refill(sums, 5, 10);
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

    /**
     * This takes the {@link #current} GreasedRegion and removes any cells that have a diagonal neighbor if that
     * neighbor cannot be accessed from shared orthogonal neighbors. That is, if a 2x2 area contains two "off" cells
     * that are diagonally adjacent and contains two "on" cells that are diagonally adjacent, this sets that whole 2x2
     * area to "off."
     * @return {@link #current} after orthogonally-inaccessible pairs of diagonal "on" cells are removed
     */
    public GreasedRegion runDiagonalGapCleanup()
    {
        neighbors[0].remake(current.not()).neighborUp();
        neighbors[1].remake(current).neighborDown();
        neighbors[2].remake(current).neighborLeft();
        neighbors[3].remake(current).neighborRight();
        neighbors[4].remake(current.not()).neighborUpLeft();
        neighbors[5].remake(current).neighborUpRight();
        neighbors[6].remake(current).neighborDownLeft();
        neighbors[7].remake(current).neighborDownRight();
//        neighbors[8].remake(current);
        current.andNot(neighbors[4].and(neighbors[0]).and(neighbors[2]));
        current.andNot(neighbors[5].and(neighbors[0]).and(neighbors[3]));
        current.andNot(neighbors[6].and(neighbors[1]).and(neighbors[2]));
        current.andNot(neighbors[7].and(neighbors[1]).and(neighbors[3]));
        return current;
    }
    
//    public static void main(String[] args)
//    {
//        GWTRNG rng = new GWTRNG(-3005655405530708008L);
//        final int bigWidth = 80, bigHeight = 48;
//        DungeonGenerator dungeonGen = new DungeonGenerator(bigWidth, bigHeight, rng);
//        dungeonGen.addWater(15);
//        dungeonGen.addGrass(10);
//        DungeonBoneGen gen = new DungeonBoneGen(rng);
//        CellularAutomaton ca = new CellularAutomaton(bigWidth, bigHeight);
//        gen.generate(TilesetType.DEFAULT_DUNGEON, bigWidth, bigHeight);
//        ca.remake(gen.region);
//        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.9);
//        gen.region.and(ca.runBasicSmoothing()).deteriorate(rng, 0.9);
//        ca.current.remake(gen.region.deteriorate(rng, 0.9));
//        gen.region.or(ca.runBasicSmoothing());
//        gen.region.remake(gen.region.removeEdges().largestPart());
//        ca.current.remake(gen.region);
//        gen.region.remake(ca.runDiagonalGapCleanup());
//        DungeonUtility.debugPrint(DungeonUtility.hashesToLines(dungeonGen.generate(gen.region.intoChars(gen.getDungeon(), '.', '#'))));
//    }
}
