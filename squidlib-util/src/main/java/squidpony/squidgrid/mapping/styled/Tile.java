package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Tile {

    public int a_constraint, b_constraint, c_constraint, d_constraint, e_constraint, f_constraint, width, height;
    public long[] data;

    /**
     * Probably not something you will construct manually. See DungeonBoneGen .
     */
    public Tile() {
        a_constraint = 0;
        b_constraint = 0;
        c_constraint = 0;
        d_constraint = 0;
        e_constraint = 0;
        f_constraint = 0;
        width = 0;
        height = 0;
        data = new long[0];
    }

    /**
     * Constructor used internally.
     *
     * @param a_constraint
     * @param b_constraint
     * @param c_constraint
     * @param d_constraint
     * @param e_constraint
     * @param f_constraint
     * @param data
     */
    public Tile(int a_constraint, int b_constraint, int c_constraint, int d_constraint, int e_constraint,
                int f_constraint, int width, int height, long... data) {
        this.a_constraint = a_constraint;
        this.b_constraint = b_constraint;
        this.c_constraint = c_constraint;
        this.d_constraint = d_constraint;
        this.e_constraint = e_constraint;
        this.f_constraint = f_constraint;
        this.width = width;
        this.height = height;
        this.data = data;
    }
}