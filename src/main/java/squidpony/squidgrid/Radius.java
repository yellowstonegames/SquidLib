package squidpony.squidgrid;

import java.awt.Point;
import squidpony.squidmath.Point3D;
import squidpony.squidmath.RNG;

/**
 * Basic radius strategy implementations likely to be used for roguelikes.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Radius {

    /**
     * In an unobstructed area the FOV would be a square.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with no additional cost for diagonal movement.
     */
    SQUARE,
    /**
     * In an unobstructed area the FOV would be a diamond.
     *
     * This is the shape that would represent movement radius in a 4-way
     * movement scheme.
     */
    DIAMOND,
    /**
     * In an unobstructed area the FOV would be a circle.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with all movement cost the same based on distance from
     * the source
     */
    CIRCLE,
    /**
     * In an unobstructed area the FOV would be a cube.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with no additional cost for diagonal movement.
     */
    CUBE,
    /**
     * In an unobstructed area the FOV would be a octahedron.
     *
     * This is the shape that would represent movement radius in a 4-way
     * movement scheme.
     */
    OCTAHEDRON,
    /**
     * In an unobstructed area the FOV would be a sphere.
     *
     * This is the shape that would represent movement radius in an 8-way
     * movement scheme with all movement cost the same based on distance from
     * the source
     */
    SPHERE;

    private static RNG rng = null;//lazy instantiation

    public double radius(int startx, int starty, int startz, int endx, int endy, int endz) {
        return radius((double) startx, (double) starty, (double) startz, (double) endx, (double) endy, (double) endz);
    }

    public double radius(double startx, double starty, double startz, double endx, double endy, double endz) {
        double dx = Math.abs(startx - endx);
        double dy = Math.abs(starty - endy);
        double dz = Math.abs(startz - endz);
        return radius(dx, dy, dz);
    }

    public double radius(int dx, int dy, int dz) {
        return radius((float) dx, (float) dy, (float) dz);
    }

    public double radius(double dx, double dy, double dz) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        dz = Math.abs(dz);
        double radius = 0;
        switch (this) {
            case SQUARE:
            case CUBE:
                radius = Math.max(dx, Math.max(dy, dz));//radius is longest axial distance
                break;
            case DIAMOND:
            case OCTAHEDRON:
                radius = dx + dy + dz;//radius is the manhattan distance
                break;
            case CIRCLE:
            case SPHERE:
                radius = Math.sqrt(dx * dx + dy * dy + dz * dz);//standard spherical radius
        }
        return radius;
    }

    public double radius(int startx, int starty, int endx, int endy) {
        return radius((double) startx, (double) starty, (double) endx, (double) endy);
    }

    public double radius(double startx, double starty, double endx, double endy) {
        double dx = Math.abs(startx - endx);
        double dy = Math.abs(starty - endy);
        return radius(dx, dy);
    }

    public double radius(int dx, int dy) {
        return radius((double) dx, (double) dy);
    }

    public double radius(double dx, double dy) {
        return radius(dx, dy, 0);
    }

    public Point onUnitShape(double distance) {
        if (rng == null) {
            rng = new RNG();
        }

        int x = 0, y = 0;
        switch (this) {
            case SQUARE:
            case CUBE:
                x = rng.between((int) -distance, (int) distance + 1);
                y = rng.between((int) -distance, (int) distance + 1);
                break;
            case DIAMOND:
            case OCTAHEDRON:
                x = rng.between((int) -distance, (int) distance + 1);
                y = rng.between((int) -distance, (int) distance + 1);
                if (radius(x, y) > distance) {
                    if (x > 0) {
                        if (y > 0) {
                            x = (int) (distance - x);
                            y = (int) (distance - y);
                        } else {
                            x = (int) (distance - x);
                            y = (int) (-distance - y);
                        }
                    } else {
                        if (y > 0) {
                            x = (int) (-distance - x);
                            y = (int) (distance - y);
                        } else {
                            x = (int) (-distance - x);
                            y = (int) (-distance - y);
                        }
                    }
                }
                break;
            case CIRCLE:
            case SPHERE:
                double radius = distance * Math.sqrt(rng.between(0.0, 1.0));
                double theta = rng.between(0, 2 * Math.PI);
                x = (int) (Math.cos(theta) * radius);
                y = (int) (Math.sin(theta) * radius);
        }

        return new Point(x, y);
    }

    public Point3D onUnitShape3D(double distance) {
        if (rng == null) {
            rng = new RNG();
        }

        int x = 0, y = 0, z = 0;
        switch (this) {
            case SQUARE:
            case DIAMOND:
            case CIRCLE:
                Point p = onUnitShape(distance);
                return new Point3D(p.x, p.y, 0);//2D strategies
            case CUBE:
                x = rng.between((int) -distance, (int) distance + 1);
                y = rng.between((int) -distance, (int) distance + 1);
                z = rng.between((int) -distance, (int) distance + 1);
                break;
            case OCTAHEDRON:
            case SPHERE:
                do {
                    x = rng.between((int) -distance, (int) distance + 1);
                    y = rng.between((int) -distance, (int) distance + 1);
                    z = rng.between((int) -distance, (int) distance + 1);
                } while (radius(x, y, z) > distance);
        }

        return new Point3D(x, y, z);
    }
    public double volume2D(double radiusLength)
    {
        switch (this) {
            case SQUARE:
            case CUBE:
                return (radiusLength * 2 + 1) * (radiusLength * 2 + 1);
            case DIAMOND:
            case OCTAHEDRON:
                return radiusLength * (radiusLength + 1) * 2 + 1;
            default:
                return Math.PI * radiusLength * radiusLength + 1;
        }
    }
    public double volume3D(double radiusLength)
    {
        switch (this) {
            case SQUARE:
            case CUBE:
                return (radiusLength * 2 + 1) * (radiusLength * 2 + 1) * (radiusLength * 2 + 1);
            case DIAMOND:
            case OCTAHEDRON:
                double total = radiusLength * (radiusLength + 1) * 2 + 1;
                for(double i = radiusLength - 1; i >= 0; i--)
                {
                    total += (i * (i + 1) * 2 + 1) * 2;
                }
                return total;
            default:
                return Math.PI * radiusLength * radiusLength * radiusLength * 4.0 / 3.0 + 1;
        }
    }
}
