package squidpony.squidmath;

import java.io.Serializable;

/**
 * Coord using double values for x and y instead of int. Not pooled.
 * When possible and you are using libGDX, use the {@code com.badlogic.gdx.math.Vector2} class in preference to this
 * one if you are OK with using floats instead of doubles.
 * <br>
 * Created by Tommy Ettinger on 8/12/2015.
 */
public class CoordDouble implements Serializable {
    private static final long serialVersionUID = 300L;
    public double x;
    public double y;

    public CoordDouble()
    {
        this(0, 0);
    }
    public CoordDouble(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    public CoordDouble(CoordDouble other)
    {
        x = other.x;
        y = other.y;
    }

    public CoordDouble(Coord other)
    {
        x = other.x;
        y = other.y;
    }
    public static CoordDouble get(double x, double y)
    {
        return new CoordDouble(x, y);
    }
    public CoordDouble getLocation()
    {
        return new CoordDouble(x, y);
    }
    public void translate(double x, double y)
    {
        this.x += x;
        this.y += y;
    }
    public void setLocation(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    public void setLocation(CoordDouble co)
    {
        x = co.x;
        y = co.y;
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
    public double distance(CoordDouble co)
    {
        return Math.sqrt((co.x - x) * (co.x - x) + (co.y - y) * (co.y - y));
    }
    public double distanceSq(double x2, double y2)
    {
        return (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
    }
    public double distanceSq(CoordDouble co)
    {
        return (co.x - x) * (co.x - x) + (co.y - y) * (co.y - y);
    }

    public double getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
	public String toString()
    {
        return "CoordDouble (x " + x + ", y " + y + ")";
    }

	@Override
	public int hashCode() {
		return 31 * (31 + NumberTools.doubleToMixedIntBits(x)) + NumberTools.doubleToMixedIntBits(y);
	}

    @Override
    public boolean equals(Object o) {
        if (o instanceof CoordDouble) {
            CoordDouble other = (CoordDouble) o;
            return x == other.x && y == other.y;
        } else {
            return false;
        }
    }

}
