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

    public Coord getLocation()
    {
        return new Coord(x, y);
    }
    public void translate(int x, int y)
    {
        this.x += x;
        this.y += y;
    }
    public void setLocation(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public void setLocation(double x, double y)
    {
        this.x = (int)(x + 0.5);
        this.y = (int)(y + 0.5);
    }
    public void setLocation(Coord co)
    {
        this.x = co.x;
        this.y = co.y;
    }
    public void move(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    public double distance(double x2, double y2)
    {
        return Math.sqrt((x2 - x) * (x2 - x) + (y2 - y) * (y2 - y));
    }
    public double distance(Coord co)
    {
        return Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }
    public double distanceSq(double x2, double y2)
    {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }
    public double distanceSq(Coord co)
    {
        return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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
