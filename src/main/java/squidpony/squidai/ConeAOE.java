package squidpony.squidai;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.mapping.DungeonUtility;

import java.awt.*;
import java.util.HashMap;
import java.util.Set;

/**
 * An AOE type that has an origin, a radius, an angle, and a span; it will blast from the origin to a length equal to
 * radius along the angle (in degrees), moving somewhat around corners/obstacles, and also spread a total of span
 * degrees around the angle (a span of 90 will affect a full quadrant, centered on angle). You can specify the
 * RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * RADIUS.CIRCLE (Euclidean measurement) will produce the most real-looking cones. This will produce doubles for its
 * getArea() method which are greater than 0.0 and less than or equal to 1.0.
 *
 * This class uses squidpony.squidgrid.FOV to create its area of effect.
 * Created by Tommy Ettinger on 7/13/2015.
 */
public class ConeAOE implements AOE {
    private FOV fov;
    private Point origin;
    private double radius, startAngle, endAngle, angle, span;
    private double[][] map;
    private Radius radiusType;
    public ConeAOE(Point origin, Point endCenter, double span, Radius radiusType)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.origin = origin;
        this.radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
        this.angle = (Math.toDegrees(Math.atan2(endCenter.y - origin.y, endCenter.x - origin.x)) % 360.0 + 360.0) % 360.0;
        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
        this.span = span;
        this.radiusType = radiusType;
    }
    public ConeAOE(Point origin, int radius, double angle, double span, Radius radiusType)
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.origin = origin;
        this.radius = radius;
        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
        this.angle = angle;
        this.span = span;
        this.radiusType = radiusType;
    }

    private ConeAOE()
    {
        fov = new FOV(FOV.RIPPLE_LOOSE);
        this.origin = new Point(1, 1);
        this.radius = 1;
        this.startAngle = 0;
        this.endAngle = 90;
        this.angle = 45;
        this.span = 90;
        this.radiusType = Radius.DIAMOND;
    }

    public Point getOrigin() {
        return origin;
    }

    public void setOrigin(Point origin) {
        this.origin = origin;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
    }

    public void setEndCenter(Point endCenter) {
        radius = radiusType.radius(origin.x, origin.y, endCenter.x, endCenter.y);
        angle = (Math.toDegrees(Math.atan2(endCenter.y - origin.y, endCenter.x - origin.x)) % 360.0 + 360.0) % 360.0;
        startAngle = Math.abs((angle - span / 2.0) % 360.0);
        endAngle = Math.abs((angle + span / 2.0) % 360.0);
    }

    public double getSpan() {
        return span;
    }

    public void setSpan(double span) {
        this.span = span;
        this.startAngle = Math.abs((angle - span / 2.0) % 360.0);
        this.endAngle = Math.abs((angle + span / 2.0) % 360.0);
    }

    public Radius getRadiusType() {
        return radiusType;
    }

    public void setRadiusType(Radius radiusType) {
        this.radiusType = radiusType;
    }

    @Override
    public void shift(Point aim) {
        setEndCenter(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets) {
            if (radiusType.radius(origin.x, origin.y, p.x, p.y) <= radius) {
                double d = ((angle - Math.toDegrees(Math.atan2(p.y - origin.y, p.x - origin.x)) % 360.0 + 360.0) % 360.0);
                if(d > 180)
                    d = 360 - d;
                if(d < span / 2.0)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void setMap(char[][] map) {
        this.map = DungeonUtility.generateResistances(map);
    }

    @Override
    public HashMap<Point, Double> findArea() {
        HashMap<Point, Double> r = AreaUtils.arrayToHashMap(fov.calculateFOV(map, origin.x, origin.y, radius,
                radiusType, startAngle, endAngle));
        r.remove(origin);
        return r;
    }
}
