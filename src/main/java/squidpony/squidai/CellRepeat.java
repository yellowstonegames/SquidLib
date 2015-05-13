package squidpony.squidai;

/**
 * A small struct-like class for determining how to repeat a cell for area of effect purposes.
 * Created by Tommy Ettinger on 5/8/2015.
 */
public class CellRepeat {
    /**
     * Constant for non-repeating cells.
     */
    public static final int SINGLE = 1;
    /**
     * Constant for cells that repeat in a straight line, moving the same distance from the last point in the line as
     * the first cell moved from the start position.
     */
    public static final int LINE = 2;
    /**
     * Constant for cells that flood-fill outward to a maximum Manhattan (4-way) distance given by length. Usually only
     * one of these cells is needed for explosion effects.
     */
    public static final int EXPAND_MANHATTAN = 4;
    /**
     * Constant for cells that flood-fill outward to a maximum Chebyshev (8-way equal weight) distance given by length.
     * Usually only one of these cells is needed for explosion effects.
     */
    public static final int EXPAND_CHEBYSHEV = 8;
    /**
     * Constant for cells that repeat moving in the same pattern as they did originally, but can change direction any
     * number of times along that path, and will choose from the set of all possible options at that point. An example
     * is the Knight's Tour chess problem, which could get a collection of all possible paths in a
     * certain number of moves by setting rotate to true, kind to FREE, and length to the desired number of moves, for
     * the two mirror-image moves that the Knight could take.
     */
    public static final int FREE = 16;
    /**
     * Defines whether all four N/S/E/W rotations should be considered as viable patterns for this CellRepeat.
     * Unless you're making some chess-like game where north is fundamentally different from west and some movement
     * can only be done in one direction of the four (as is the case for Pawn movement), this should probably
     * stay true (the default).
     */
    public boolean rotate = true;
    /**
     * This should equal one of the five constants in this class for repeat type: SINGLE, LINE, EXPAND_MANHATTAN,
     * EXPAND_CHEBYSHEV, or FREE.
     */
    public int kind = SINGLE;
    /**
     * The maximum number of times to repeat the travel of the cell. Ignored for kind=SINGLE, used as radius for the
     * two EXPAND_ kinds, and the limit on iteration for FREE. If length is too high for FREE mode, you should expect
     * stack overflow errors, so don't try an unlimited free movement.
     */
    public int length = 1;

    /**
     * Creates a new CellRepeat with the default values (rotate=true, kind=SINGLE, length=1).
     */
    public CellRepeat()
    {

    }

    /**
     * Creates a new CellRepeat with the supplied values for kind and length, but rotate set to the default (true).
     */
    public CellRepeat(int kind, int length)
    {
        this.kind = kind;
        this.length = length;
    }
    /**
     * Creates a new CellRepeat with the supplied values for kind, length, and rotate.
     */
    public CellRepeat(int kind, int length, boolean rotate)
    {
        this.kind = kind;
        this.length = length;
        this.rotate = rotate;
    }

    /**
     * Every CellRepeat created with the parameter-less constructor is identical, so unless you alter DEFAULT,
     * you can use this field instead of constructing new CellRepeat-s for every cell in a pattern.
     */
    public static final CellRepeat DEFAULT = new CellRepeat();
    /**
     * Area of Effect patterns don't usually need 4-way rotation around the center of their effect, and this otherwise
     * will act like the default constructor.
     * You can use this field instead of constructing new CellRepeat-s for every cell in a pattern.
     */
    public static final CellRepeat DEFAULT_AOE = new CellRepeat(SINGLE, 1, false);
}
