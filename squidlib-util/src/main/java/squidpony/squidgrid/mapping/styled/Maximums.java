package squidpony.squidgrid.mapping.styled;

/**
 * Part of the JSON that defines a tileset.
 * Created by Tommy Ettinger on 3/10/2015.
 */
public class Maximums {
    public int h, v;

    /**
     * Probably not something you will construct manually. See DungeonGen .
     */
    public Maximums() {
        h = 64;
        v = 64;
    }

    public Maximums(int h, int v) {
        this.h = h;
        this.v = v;
    }

    public String toString()
    {
        return "new Maximums(" + h + ", " + v + ")";
    }
}
