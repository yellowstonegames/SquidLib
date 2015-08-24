package squidpony.squidgrid;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides methods for calculating Field of View in grids. Field of
 * View (FOV) algorithms determine how much area surrounding a point can be
 * seen. They return a two dimensional array of doubles, representing the amount
 * of view (typically sight, but perhaps sound, smell, etc.) which the origin
 * has of each cell.
 *
 * After a calculation has been run, the resulting light map is saved in the
 * class. This allows the values to be checked on individual cells without being
 * required to work with the returned light map. If no calculation has been
 * performed, all value checking methods will return their version of "unlit".
 *
 * The input resistanceMap is considered the percent of opacity. This resistance
 * is on top of the resistance applied from the light spreading out.
 *
 * The returned light map is considered the percent of light in the cells.
 *
 * Not all implementations are required to provide percentage levels of light.
 * In such cases the returned values will be 0 for no light and 1.0 for fully
 * lit. Implementations that return this way note so in their documentation.
 *
 * All solvers perform bounds checking so solid borders in the map are not
 * required.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOV {

    public static final int//
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will go around corners a bit.
             */
            RIPPLE = 1,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will spread around edges like smoke or water, but maintain a
             * tendency to curl towards the start position when going around
             * edges.
             */
            RIPPLE_LOOSE = 2,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will only go around corners slightly.
             */
            RIPPLE_TIGHT = 3,
            /**
             * Performs FOV by pushing values outwards from the source location.
             * It will go around corners massively.
             */
            RIPPLE_VERY_LOOSE = 4,
            /**
             * Uses Shadow Casting FOV algorithm. Treats all translucent cells
             * as fully transparent. Returns only that the cell is fully lit or
             * not lit, does not do percentages.
             */
            SHADOW = 5;

    private double lightMap[][], map[][];
    private boolean indirect[][];//marks indirect lighting for Ripple FOV
    private int type = SHADOW;
    private int rippleNeighbors;
    private double radius, decay, angle, span;
    private int startx, starty, width, height;
    private Radius radiusStrategy;
    private Comparator<Point> comp;
    private static Direction[] ccw = new Direction[]
            {Direction.UP_RIGHT, Direction.UP_LEFT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT, Direction.UP_RIGHT},
            ccw_full = new Direction[]{Direction.RIGHT, Direction.UP_RIGHT, Direction.UP, Direction.UP_LEFT,
            Direction.LEFT, Direction.DOWN_LEFT, Direction.DOWN, Direction.DOWN_RIGHT};

    /**
     * Creates a solver which will use the default SHADOW solver.
     */
    public FOV() {
        lightMap = null;
        map = null;
    }

    /**
     * Creates a solver which will use the provided FOV solver type.
     *
     * @param type
     */
    public FOV(int type) {
        lightMap = null;
        map = null;
        this.type = type;
    }

    /**
     * Checks to see if the location is considered even partially lit.
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isLit(int x, int y) {
        return lightLevel(x, y) > 0;
    }

    /**
     * Returns the light value at the given location.
     *
     * @param x
     * @param y
     * @return
     */
    public double lightLevel(int x, int y) {
        if (lightMap == null) {
            return 0;
        } else {
            return lightMap[x][y];
        }
    }

    /**
     * Returns the initial resistance value at the given point. If no
     * calculation has yet been run, it returns 0.
     *
     * @param x
     * @param y
     * @return
     */
    public double resistance(int x, int y) {
        if (map == null) {
            return 0;
        } else {
            return map[x][y];
        }
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty) {
        return calculateFOV(resistanceMap, startx, starty, Integer.MAX_VALUE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidian
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startx, int starty, double radius) {
        return calculateFOV(resistanceMap, startx, starty, radius, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius, Radius radiusTechnique) {
        this.map = resistanceMap;
        this.startx = startX;
        this.starty = startY;
        this.radius = Math.max(1, radius);
        this.radiusStrategy = radiusTechnique;
        this.comp = new Comparator<Point>() {
            @Override
            public int compare(Point pt1, Point pt2) {
                return (int)Math.signum(radiusStrategy.radius(startx, starty, pt1.x, pt1.y) -
                        radiusStrategy.radius(startx, starty, pt2.x, pt2.y));
            }
        };
        decay = 1.0 / radius;

        width = resistanceMap.length;
        height = resistanceMap[0].length;

        lightMap = new double[width][height];
        lightMap[startx][starty] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
                indirect = new boolean[width][height];
                rippleNeighbors = 2;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 3;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_TIGHT:
                indirect = new boolean[width][height];
                rippleNeighbors = 1;
                doRippleFOV(startx, starty);
                break;
            case RIPPLE_VERY_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 6;
                doRippleFOV(startx, starty);
                break;
            case SHADOW:
               	// hotfix for infinite radius -> set to longest possible straight-line Manhattan distance instead
                // does not cause problems with brightness falloff because shadowcasting is on/off
            	// TODO do proper fix for shadowCast
            	if (radius >= Integer.MAX_VALUE){
            		this.radius = width + height;
            	}
                for (Direction d : Direction.DIAGONALS) {
                    shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0);
                    shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY);
                }
                break;
        }

        return lightMap;
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. A conical section of FOV is lit by this method if
     * startAngle and endAngle are not equal.
     *
     * @param resistanceMap the grid of cells to calculate on
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius,
                                   Radius radiusTechnique, double angle, double span) {
        this.map = resistanceMap;
        this.startx = startX;
        this.starty = startY;
        this.radius = Math.max(1, radius);

        this.angle = Math.toRadians((angle > 360.0 || angle < 0.0) ? Math.IEEEremainder(angle + 720.0, 360.0) : angle);
        this.span = Math.toRadians(span);
        this.radiusStrategy = radiusTechnique;
        decay = 1.0 / radius;
        this.comp = new Comparator<Point>() {
            @Override
            public int compare(Point pt1, Point pt2) {
                return (int)Math.signum(radiusStrategy.radius(startx, starty, pt1.x, pt1.y) -
                        radiusStrategy.radius(startx, starty, pt2.x, pt2.y));
            }
        };
        width = resistanceMap.length;
        height = resistanceMap[0].length;

        lightMap = new double[width][height];
        lightMap[startx][starty] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
                indirect = new boolean[width][height];
                rippleNeighbors = 2;
                doRippleFOVLimited(startx, starty);
                break;
            case RIPPLE_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 3;
                doRippleFOVLimited(startx, starty);
                break;
            case RIPPLE_TIGHT:
                indirect = new boolean[width][height];
                rippleNeighbors = 1;
                doRippleFOVLimited(startx, starty);
                break;
            case RIPPLE_VERY_LOOSE:
                indirect = new boolean[width][height];
                rippleNeighbors = 6;
                doRippleFOVLimited(startx, starty);
                break;
            case SHADOW:
                int ctr = 0;
                boolean started = false;
                for (Direction d : ccw) {
                    ctr %= 4;
                    ++ctr;
                    if (angle <= Math.PI / 2.0 * ctr + span / 2.0)
                        started = true;
                    if (started) {
                        if(ctr < 4 && angle < Math.PI / 2.0 * (ctr - 1) - span / 2.0)
                            break;
                        shadowCastLimited(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0);
                        shadowCastLimited(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY);
                    }
                }
                break;
        }

        return lightMap;
    }


    private void doRippleFOV(int x, int y) {
        Deque<Point> dq = new LinkedList<>();
        dq.offer(new Point(x, y));
        while (!dq.isEmpty()) {
            Point p = dq.pop();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : Direction.OUTWARDS) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }

                double surroundingLight = nearRippleLight(x2, y2);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(new Point(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }


    private void doRippleFOVLimited(int x, int y) {
        Deque<Point> dq = new LinkedList<>();
        dq.offer(new Point(x, y));
        while (!dq.isEmpty()) {
            Point p = dq.pop();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : ccw_full) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {  //+1 to cover starting tile
                    continue;
                }
                double newAngle = Math.atan2(y2 - starty, x2 - startx) + Math.PI * 2;
                if(Math.abs(Math.IEEEremainder(angle - newAngle, Math.PI * 2)) > span / 2.0) continue;

                double surroundingLight = nearRippleLight(x2, y2);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(new Point(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private double nearRippleLight(int x, int y) {
        if (x == startx && y == starty) {
            return 1;
        }

        List<Point> neighbors = new ArrayList<>();
        for (Direction di : Direction.OUTWARDS) {
            int x2 = x + di.deltaX;
            int y2 = y + di.deltaY;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                neighbors.add(new Point(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }
        Collections.sort(neighbors, comp);
        neighbors = neighbors.subList(0, rippleNeighbors);
/*
        while (neighbors.size() > rippleNeighbors) {
            Point p = neighbors.remove(0);
            double dist = radiusStrategy.radius(startx, starty, p.x, p.y);
            double dist2 = 0;
            for (Point p2 : neighbors) {
                dist2 = Math.max(dist2, radiusStrategy.radius(startx, starty, p2.x, p2.y));
            }
            if (dist < dist2) {//not the largest, put it back
                neighbors.add(p);
            }
        }
*/
        double light = 0;
        int lit = 0, indirects = 0;
        for (Point p : neighbors) {
            if (lightMap[p.x][p.y] > 0) {
                lit++;
                if (indirect[p.x][p.y]) {
                    indirects++;
                }
                double dist = radiusStrategy.radius(x, y, p.x, p.y);
                light = Math.max(light, lightMap[p.x][p.y] - dist * decay - map[p.x][p.y]);
            }
        }

        if (map[x][y] >= 1 || indirects >= lit) {
            indirect[x][y] = true;
        }
        return light;
    }

    private void shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy) {
        double newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }


                //check if it's within the lightable area and light if needed
                if (radiusStrategy.radius(deltaX, deltaY) <= radius) {
                    double bright = 1 - decay * radiusStrategy.radius(deltaX, deltaY);
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }
    private void shadowCastLimited(int row, double start, double end, int xx, int xy, int yx, int yy) {
        double newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < this.width && currentY < this.height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double newAngle = Math.atan2(currentY - starty, currentX - startx) + Math.PI * 2;
                if(Math.abs(Math.IEEEremainder(angle - newAngle, Math.PI * 2)) > span / 2.0) continue;

                //check if it's within the lightable area and light if needed
                if (radiusStrategy.radius(deltaX, deltaY) <= radius) {
                    double bright = 1 - decay * radiusStrategy.radius(deltaX, deltaY);
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }
}
