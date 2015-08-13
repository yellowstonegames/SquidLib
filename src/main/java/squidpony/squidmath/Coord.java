package squidpony.squidmath;

/**
 * Created by Tommy Ettinger on 8/12/2015.
 */
public class Coord implements java.io.Serializable {
    private static final long serialVersionUID = 300L;
    public int x;
    public int y;

    public Coord()
    {
        this(0, 0);
    }
    public Coord(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public Coord(Coord other)
    {
        this.x = other.x;
        this.y = other.y;
    }
    public String toString()
    {
        return "Coord (x " + x + ", y " + y + ")";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 113 * hash + x;
        hash = 113 * hash + y;
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Coord) {
            Coord other = (Coord) o;
            return x == other.x && y == other.y;
        } else {
            return false;
        }
    }

}
