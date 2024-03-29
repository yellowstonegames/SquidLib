/*
 * Copyright (c) 2022 Eben Howard, Tommy Ettinger, and contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
    private static final long serialVersionUID = 500L;
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

    /**
     * Constructs an identical copy to this CoordDouble, making a new object that may be mutated independently.
     * @return a copy of this CoordDouble
     */
    public CoordDouble copy()
    {
        return new CoordDouble(x, y);
    }

    public CoordDouble add(double x, double y)
    {
        this.x += x;
        this.y += y;
        return this;
    }

    public CoordDouble add(CoordDouble other)
    {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

    public CoordDouble subtract(double x, double y)
    {
        this.x -= x;
        this.y -= y;
        return this;
    }

    public CoordDouble subtract(CoordDouble other)
    {
        this.x -= other.x;
        this.y -= other.y;
        return this;
    }

    public CoordDouble multiply(double x, double y)
    {
        this.x *= x;
        this.y *= y;
        return this;
    }

    public CoordDouble multiply(CoordDouble other)
    {
        this.x *= other.x;
        this.y *= other.y;
        return this;
    }

    /**
     * Divides the x component of this CoordDouble by {@code x} and the y component by {@code y}. Be careful about when
     * either of the parameters can be 0.0, since that can put NaN or infinite components in this.
     * @param x divisor for x
     * @param y divisor for y
     */
    public CoordDouble divide(double x, double y)
    {
        this.x /= x;
        this.y /= y;
        return this;
    }

    /**
     * Divides the x component of this CoordDouble by {@code other.x} and the y component by {@code other.y}. Be careful
     * about when either of other's components can be 0.0, since that can put NaN or infinite components in this.
     * @param other a non-null CoordDouble to get divisors from
     */
    public CoordDouble divide(CoordDouble other)
    {
        this.x /= other.x;
        this.y /= other.y;
        return this;
    }

    /**
     * Gets the dot product of this CoordDouble and {@code other}.
     * @param other another CoordDouble; must not be null.
     * @return the dot product of this and {@code other}.
     */
    public double dot(CoordDouble other)
    {
        return x * other.x + y * other.y;
    }

    /**
     * Gets the cross product of this CoordDouble and {@code other}.
     * @param other another CoordDouble; must not be null.
     * @return the cross product of this and {@code other}.
     */
    public double cross(CoordDouble other)
    {
        return y * other.x - x * other.y;
    }
    
    public CoordDouble set(double x, double y)
    {
        this.x = x;
        this.y = y;
        return this;
    }
    public CoordDouble set(CoordDouble co)
    {
        x = co.x;
        y = co.y;
        return this;
    }
    /**
     * Distance from the origin to this CoordDouble.
     * @return the distance from the origin to this CoordDouble.
     */
    public double length()
    {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Distance from the origin to this CoordDouble, squared.
     * @return the distance from the origin to this CoordDouble, squared.
     */
    public double lengthSq()
    {
        return (x * x + y * y);
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

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
	public String toString()
    {
        return "CoordDouble (x " + x + ", y " + y + ")";
    }

	@Override
	public int hashCode() {
		return 31 * (31 + NumberTools.doubleToMixedIntBits(x)) + NumberTools.doubleToMixedIntBits(y) | 0;
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
