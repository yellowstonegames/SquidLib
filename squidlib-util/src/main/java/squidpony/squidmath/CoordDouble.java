package squidpony.squidmath;

/**
 * Coord using double values for x and y instead of int. Not pooled.
 * Created by Tommy Ettinger on 8/12/2015.
 */
public class CoordDouble implements java.io.Serializable {
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
        return "Coord (x " + x + ", y " + y + ")";
    }

	@Override
	/*
	 * smelC: This is Eclipse-generated code. The previous version was
	 * Gwt-incompatible (because of Double.doubleToRawLongBits).
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
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
