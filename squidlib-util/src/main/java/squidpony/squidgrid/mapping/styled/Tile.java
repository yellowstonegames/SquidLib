package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Tile {
    public int a_constraint, b_constraint, c_constraint, d_constraint, e_constraint, f_constraint;
    public String[] data;

    /**
     * Probably not something you will construct manually. See DungeonGen .
     */
    public Tile() {
        a_constraint = 0;
        b_constraint = 0;
        c_constraint = 0;
        d_constraint = 0;
        e_constraint = 0;
        f_constraint = 0;
        data = new String[]{};
    }
}