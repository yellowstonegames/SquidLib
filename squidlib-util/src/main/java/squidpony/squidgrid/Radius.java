package squidpony.squidgrid;

import squidpony.squidmath.Coord;
import squidpony.squidmath.Coord3D;
import squidpony.squidmath.RNG;

import java.util.LinkedHashSet;
import java.util.Set;

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

    private static final double PI2 = Math.PI * 2;
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
    public double radius(Coord start, Coord end) {
        return radius((double) start.x, (double) start.y, (double) end.x, (double) end.y);
    }
    public double radius(Coord end) {
        return radius(0.0, 0.0, (double) end.x, (double) end.y);
    }

    public double radius(double startx, double starty, double endx, double endy) {
        double dx = startx - endx;
        double dy = starty - endy;
        return radius(dx, dy);
    }

    public double radius(int dx, int dy) {
        return radius((double) dx, (double) dy);
    }

    public double radius(double dx, double dy) {
        return radius(dx, dy, 0);
    }

    public Coord onUnitShape(double distance, RNG rng) {
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
                double theta = rng.between(0, PI2);
                x = (int) Math.round(Math.cos(theta) * radius);
                y = (int) Math.round(Math.sin(theta) * radius);
        }

        return Coord.get(x, y);
    }

    public Coord3D onUnitShape3D(double distance, RNG rng) {
        int x = 0, y = 0, z = 0;
        switch (this) {
            case SQUARE:
            case DIAMOND:
            case CIRCLE:
                Coord p = onUnitShape(distance, rng);
                return new Coord3D(p.x, p.y, 0);//2D strategies
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

        return new Coord3D(x, y, z);
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




    private int clamp(int n, int min, int max)
    {
        return Math.min(Math.max(min, n), max - 1);
    }

    public Set<Coord> perimeter(Coord center, int radiusLength, boolean surpassEdges, int width, int height)
    {
        LinkedHashSet<Coord> rim = new LinkedHashSet<>(4 * radiusLength);
        if(!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height))
            return rim;
        if(radiusLength < 1) {
            rim.add(center);
            return rim;
        }
        switch (this) {
            case SQUARE:
            case CUBE:
            {
                for (int i = center.x - radiusLength; i <= center.x + radiusLength; i++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y - radiusLength, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y + radiusLength, 0, height)));
                }
                for (int j = center.y - radiusLength; j <= center.y + radiusLength; j++) {
                    int y = j;
                    if(!surpassEdges) y = clamp(j, 0, height);
                    rim.add(Coord.get(clamp(center.x - radiusLength, 0, height), y));
                    rim.add(Coord.get(clamp(center.x + radiusLength, 0, height), y));
                }
            }
            break;
            case DIAMOND:
            case OCTAHEDRON: {
                int xUp = center.x + radiusLength, xDown = center.x - radiusLength,
                        yUp = center.y + radiusLength, yDown = center.y - radiusLength;
                if(!surpassEdges) {
                    xDown = clamp(xDown, 0, width);
                    xUp = clamp(xUp, 0, width);
                    yDown = clamp(yDown, 0, height);
                    yUp = clamp(yUp, 0, height);
                }

                rim.add(Coord.get(xDown, center.y));
                rim.add(Coord.get(xUp, center.y));
                rim.add(Coord.get(center.x, yDown));
                rim.add(Coord.get(center.x, yUp));

                for (int i = xDown + 1, c = 1; i < center.x; i++, c++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y - c, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y + c, 0, height)));
                }
                for (int i = center.x + 1, c = 1; i < center.x + radiusLength; i++, c++) {
                    int x = i;
                    if(!surpassEdges) x = clamp(i, 0, width);
                    rim.add(Coord.get(x, clamp(center.y + radiusLength - c, 0, height)));
                    rim.add(Coord.get(x, clamp(center.y - radiusLength + c, 0, height)));
                }
            }
            break;
            default:
            {
                double theta = PI2;
                int x, y, denom = 1;
                boolean anySuccesses = false;
                while(denom <= 256) {
                    anySuccesses = false;
                    for (int i = 1; i <= denom; i+=2)
                    {
                        theta = i * (PI2 / denom);
                        x = (int) Math.round(Math.cos(theta) * radiusLength) + center.x;
                        y = (int) Math.round(Math.sin(theta) * radiusLength) + center.y;
                        if (!surpassEdges) {
                            x = clamp(x, 0, width);
                            y = clamp(y, 0, height);
                        }
                        Coord p = Coord.get(x, y);
                        boolean test = rim.contains(p);

                        rim.add(p);
                        anySuccesses = test || anySuccesses;
                    }
                    if(!anySuccesses)
                        break;
                    denom *= 2;
                }

            }
        }
        return rim;
    }
    public Coord extend(Coord center, Coord middle, int radiusLength, boolean surpassEdges, int width, int height)
    {
        if(!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height ||
                middle.x < 0 || middle.x >= width || middle.y < 0 || middle.y > height))
            return Coord.get(0, 0);
        if(radiusLength < 1) {
            return center;
        }
        double theta = Math.atan2(middle.y - center.y, middle.x - center.x);

        Coord end = Coord.get(middle.x, middle.y);
        switch (this) {
            case SQUARE:
            case CUBE:
            case DIAMOND:
            case OCTAHEDRON:
            {
                int rad2 = 0;
                if(surpassEdges)
                {
                    while (radius(center.x, center.y, end.x, end.y) < radiusLength) {
                        rad2++;
                        end = Coord.get((int) Math.round(Math.cos(theta) * rad2) + center.x
                                , (int) Math.round(Math.sin(theta) * rad2) + center.y);
                    }
                }
                else {
                    while (radius(center.x, center.y, end.x, end.y) < radiusLength) {
                        rad2++;
                        end = Coord.get(clamp((int) Math.round(Math.cos(theta) * rad2) + center.x, 0, width)
                                      , clamp((int) Math.round(Math.sin(theta) * rad2) + center.y, 0, height));
                        if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1)
                            return end;
                    }
                }

                return end;
            }
            default:
            {
                end = Coord.get(clamp( (int) Math.round(Math.cos(theta) * radiusLength) + center.x, 0, width)
                        ,clamp( (int) Math.round(Math.sin(theta) * radiusLength) + center.y, 0, height));
                if(!surpassEdges) {
                    long edgeLength = 0;
//                    if (end.x == 0 || end.x == width - 1 || end.y == 0 || end.y == height - 1)
                    if (end.x < 0)
                    {
                        // wow, we lucked out here. the only situation where cos(angle) is 0 is if the angle aims
                        // straight up or down, and then x cannot be < 0 or >= width.
                        edgeLength = Math.round((0 - center.x) / Math.cos(theta));
                        end = end.setY(clamp((int) Math.round(Math.sin(theta) * edgeLength) + center.y, 0, height));
                    }
                    else if(end.x >= width)
                    {
                        // wow, we lucked out here. the only situation where cos(angle) is 0 is if the angle aims
                        // straight up or down, and then x cannot be < 0 or >= width.
                        edgeLength = Math.round((width - 1 - center.x) / Math.cos(theta));
                        end = end.setY(clamp((int) Math.round(Math.sin(theta) * edgeLength) + center.y, 0, height));
                    }

                    if (end.y < 0)
                    {
                        // wow, we lucked out here. the only situation where sin(angle) is 0 is if the angle aims
                        // straight left or right, and then y cannot be < 0 or >= height.
                        edgeLength = Math.round((0 - center.y) / Math.sin(theta));
                        end = end.setX(clamp((int) Math.round(Math.cos(theta) * edgeLength) + center.x, 0, width));
                    }
                    else if(end.y >= height)
                    {
                        // wow, we lucked out here. the only situation where sin(angle) is 0 is if the angle aims
                        // straight left or right, and then y cannot be < 0 or >= height.
                        edgeLength = Math.round((height - 1 - center.y) / Math.sin(theta));
                        end = end.setX(clamp((int) Math.round(Math.cos(theta) * edgeLength) + center.x, 0, width));
                    }
                }
                return end;
            }
        }
    }

    /**
     * Compares two Radius enums as if they are both in a 2D plane; that is, Radius.SPHERE is treated as equal to
     * Radius.CIRCLE, Radius.CUBE is equal to Radius.SQUARE, and Radius.OCTAHEDRON is equal to Radius.DIAMOND.
     * @param other the Radius to compare this to
     * @return true if the 2D versions of both Radius enums are the same shape.
     */
    public boolean equals2D(Radius other)
    {
        switch (this)
        {
            case CIRCLE:
            case SPHERE:
                return (other == CIRCLE || other == SPHERE);
            case SQUARE:
            case CUBE:
                return (other == SQUARE || other == CUBE);
            default:
                return (other == DIAMOND || other == OCTAHEDRON);
        }
    }
    public boolean inRange(int startx, int starty, int endx, int endy, int minRange, int maxRange)
    {
        double dist = radius(startx, starty, endx, endy);
        return dist >= minRange - 0.001 && dist <= maxRange + 0.001;
    }

    public int roughDistance(int xPos, int yPos) {
        int x = Math.abs(xPos), y = Math.abs(yPos);
        switch (this) {
            case CIRCLE:
            case SPHERE:
            {
                if(x == y)
                    return 3 * x;
                else if(x < y)
                    return 3 * x + 2 * (y - x);
                else
                    return 3 * y + 2 * (x - y);
            }
            case DIAMOND:
            case OCTAHEDRON:
                return 2 * (x + y);
            default:
                return 2 * Math.max(x, y);
        }
    }

    public Set<Coord> pointsInside(Coord center, int radiusLength, boolean surpassEdges, int width, int height)
    {
        LinkedHashSet<Coord> contents = new LinkedHashSet<Coord>((int)Math.ceil(volume2D(radiusLength)));
        if(!surpassEdges && (center.x < 0 || center.x >= width || center.y < 0 || center.y > height))
            return contents;
        if(radiusLength < 1) {
            contents.add(center);
            return contents;
        }
        switch (this) {
            case SQUARE:
            case CUBE:
            {
                for (int i = center.x - radiusLength; i <= center.x + radiusLength; i++) {
                    for (int j = center.y - radiusLength; j <= center.y + radiusLength; j++) {
                        if(!surpassEdges && (i < 0 || j < 0 || i >= width || j >= height))
                            continue;
                        contents.add(Coord.get(i, j));
                    }
                }
            }
            break;
            case DIAMOND:
            case OCTAHEDRON: {
                for (int i = center.x - radiusLength; i <= center.x + radiusLength; i++) {
                    for (int j = center.y - radiusLength; j <= center.y + radiusLength; j++) {
                        if ((Math.abs(center.x - i) + Math.abs(center.y- j) > radiusLength) ||
                                (!surpassEdges && (i < 0 || j < 0 || i >= width || j >= height)))
                            continue;
                        contents.add(Coord.get(i, j));
                    }
                }
            }
            break;
            default:
            {
                int high;
                for (int dx = -radiusLength; dx <= radiusLength; ++dx) {
                    high = (int) Math.floor(Math.sqrt(radiusLength * radiusLength - dx * dx));
                    for (int dy = -high; dy <= high; ++dy) {
                        if (!surpassEdges && (center.x + dx < 0 || center.y + dy < 0 ||
                                center.x + dx >= width || center.y + dy >= height))
                            continue;
                        contents.add(Coord.get(center.x + dx, center.y + dy));
                    }
                }
            }
        }
        return contents;

    }

}
