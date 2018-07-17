package squidpony.squidgrid;

import squidpony.ArrayTools;
import squidpony.squidmath.Coord;
import squidpony.squidmath.MathExtras;
import squidpony.squidmath.NumberTools;

import java.io.Serializable;
import java.util.*;

/**
 * This class provides methods for calculating Field of View in grids. Field of
 * View (FOV) algorithms determine how much area surrounding a point can be
 * seen. They return a two dimensional array of doubles, representing the amount
 * of view (typically sight, but perhaps sound, smell, etc.) which the origin
 * has of each cell.
 * <br>
 * The input resistanceMap is considered the percent of opacity. This resistance
 * is on top of the resistance applied from the light spreading out. You can
 * obtain a resistance map easily with the DungeonUtility.generateResistances()
 * method, which uses defaults for common chars used in SquidLib, but you may
 * also want to create a resistance map manually if a given char means something
 * very different in your game. This is easy enough to do by looping over all the
 * x,y positions in your char[][] map and running a switch statement on each char,
 * assigning a double to the same x,y position in a double[][]. The value should
 * be between 0.0 (unblocked) for things light passes through, 1.0 (blocked) for
 * things light can't pass at all, and possibly other values if you have
 * translucent obstacles.
 * <br>
 * The returned light map is considered the percent of light in the cells.
 * <br>
 * Not all implementations are required to provide percentage levels of light.
 * In such cases the returned values will be 0 for no light and 1.0 for fully
 * lit. Implementations that return this way note so in their documentation.
 * Currently, all implementations do provide percentage levels.
 * <br>
 * All solvers perform bounds checking so solid borders in the map are not
 * required.
 * <br>
 * Static methods are provided to add together FOV maps in the simple way
 * (disregarding visibility of distant FOV from a given cell), or the more
 * practical way for roguelikes (where a cell needs to be within line-of-sight
 * in the first place for a distant light to illuminate it). The second method
 * relies on an LOS map, which is essentially the same as a very-high-radius
 * FOV map and can be easily obtained with calculateLOSMap().
 * <br>
 * If you want to iterate through cells that are visible in a double[][] returned
 * by FOV, you can pass that double[][] to the constructor for Region, and you
 * can use the Region as a reliably-ordered List of Coord (among other things).
 * The order Region iterates in is somewhat strange, and doesn't, for example,
 * start at the center of an FOV map, but it will be the same every time you
 * create a Region with the same FOV map (or the same visible Coords).
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOV implements Serializable {
    private static final long serialVersionUID = 3258723684733275798L;

    public static final int
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
             * as fully transparent. Returns a percentage from 1.0 (center of
             * FOV) to 0.0 (outside of FOV).
             */
            SHADOW = 5;
    private int type = SHADOW;

	/**
	 * Data allocated in the previous calls to the public API, if any. Used to
	 * save allocations when multiple calls are done on the same instance.
	 */
    protected double[][] light;
	/**
	 * Data allocated in the previous calls to the public API, if any. Used to
	 * save allocations when multiple calls are done on the same instance.
	 */
    protected boolean[][] nearLight;

    protected static final Direction[] ccw = new Direction[]
            {Direction.UP_RIGHT, Direction.UP_LEFT, Direction.DOWN_LEFT, Direction.DOWN_RIGHT, Direction.UP_RIGHT},
            ccw_full = new Direction[]{Direction.RIGHT, Direction.UP_RIGHT, Direction.UP, Direction.UP_LEFT,
            Direction.LEFT, Direction.DOWN_LEFT, Direction.DOWN, Direction.DOWN_RIGHT};

    /**
     * Creates a solver which will use the default SHADOW solver.
     */
    public FOV() {
    }

    /**
     * Creates a solver which will use the provided FOV solver type.
     *
     * @param type
     */
    public FOV(int type) {
        this.type = type;
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
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
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
     * of the origin cell. Radius determinations based on Euclidean
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
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
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid
     */
    public double[][] calculateFOV(double[][] resistanceMap, int startX, int startY, double radius, Radius radiusTechnique) {
        double rad = Math.max(1, radius);
        double decay = 1.0 / rad;

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        initializeLightMap(width, height);
        light[startX][startY] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
            case RIPPLE_LOOSE:
            case RIPPLE_TIGHT:
            case RIPPLE_VERY_LOOSE:
                initializeNearLight(width, height);
                doRippleFOV(light, rippleValue(type), startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, radiusTechnique);
                break;
            case SHADOW:
                for (Direction d : Direction.DIAGONALS) {
                    shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, light, resistanceMap, radiusTechnique);
                    shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, light, resistanceMap, radiusTechnique);
                }
                break;
        }
        return light;
    }

	/**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Returns a light map where the values represent a percentage
     * of fully lit.
     *
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
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

        radius = Math.max(1, radius);

        double decay = 1.0 / (radius + 1);

        angle = ((angle >= 360.0 || angle < 0.0)
                ? MathExtras.remainder(angle, 360.0) : angle) * 0.002777777777777778;
        span = span * 0.002777777777777778;
        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        initializeLightMap(width, height);
        light[startX][startY] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
            case RIPPLE_LOOSE:
            case RIPPLE_TIGHT:
            case RIPPLE_VERY_LOOSE:
                initializeNearLight(width, height);
                doRippleFOV(light, rippleValue(type), startX, startY, startX, startY, decay, radius, resistanceMap, nearLight, radiusTechnique, angle, span);
                break;
            case SHADOW:
                light = shadowCastLimited(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
                light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

                light = shadowCastLimited(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
                light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

                light = shadowCastLimited(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
                light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

                light = shadowCastLimited(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
                light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
                break;
        }

        return light;
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter.  The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid
     */
    public static double[][] calculateFOV(double[][] resistanceMap, double[][] light, int startx, int starty) {
        return reuseFOV(resistanceMap, light, startx, starty, Integer.MAX_VALUE, Radius.CIRCLE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startx, int starty, double radius) {
        return reuseFOV(resistanceMap, light, startx, starty, radius, Radius.CIRCLE);
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to calculate the radius as desired
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY, double radius, Radius radiusTechnique)
    {
        radius = Math.max(1, radius);
        double decay = 1.0 / radius;
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power


        shadowCast(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);

        shadowCast(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        shadowCast(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique);
        return light;
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light map where the values
     * are always either 0.0 for "not in line of sight" or 1.0 for "in line of
     * sight," which doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses shadowcasting FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are pretty much irrelevant because
     * the distance doesn't matter, only the presence of a clear line, but this uses
     * {@link Radius#SQUARE} if it matters.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "no line"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static double[][] reuseLOS(double[][] resistanceMap, double[][] light, int startX, int startY)
    {
        double radius = light.length + light[0].length;
        double decay = 1.0 / radius;
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        
        shadowCast(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        shadowCast(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        
        shadowCast(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        shadowCast(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        
        shadowCast(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        shadowCast(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        
        shadowCast(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        shadowCast(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
        
        return light;
    }
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting at the given angle in  degrees and covering a span
     * centered on that angle, also in degrees. Assigns to, and returns, a light
     * map where the values represent a percentage of fully lit. Always uses
     * shadowcasting FOV, which allows this method to be static since it doesn't
     * need to keep any state around, and can reuse the state the user gives it
     * via the {@code light} parameter. The values in light are cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.  A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param radiusTechnique provides a means to shape the FOV by changing distance calculation (circle, square, etc.)
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY,
                                          double radius, Radius radiusTechnique, double angle, double span) {

        radius = Math.max(1, radius);
        double decay = 1.0 / (radius + 1.0);
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        angle = ((angle >= 360.0 || angle < 0.0)
                ? MathExtras.remainder(angle, 360.0) : angle) * 0.002777777777777778;
        span = span * 0.002777777777777778;


        light = shadowCastLimited(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, radiusTechnique, angle, span);
        return light;
    }

    /**
     * Reuses the existing light 2D array and fills it with a straight-line bouncing path of light that reflects its way
     * through the given resistanceMap from startX, startY until it uses up the given distance. The angle the path
     * takes is given in degrees, and the angle used can change as obstacles are hit (reflecting backwards if it hits a
     * corner pointing directly into or away from its path). This can be used something like an LOS method, but because
     * the path can be traveled back over, an array or Queue becomes somewhat more complex, and the decreasing numbers
     * for a straight line that stack may make more sense for how this could be used (especially with visual effects).
     * This currently allows the path to pass through single-cell wall-like obstacles without changing direction, e.g.
     * it passes through pillars, but will bounce if it hits a bigger wall.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param distance the distance the light will extend to
     * @param angle in degrees, the angle to start the path traveling in
     * @return the given light parameter, after modifications
     */
    public static double[][] bouncingLine(double[][] resistanceMap, double[][] light, int startX, int startY, double distance, double angle)
    {
        double rad = Math.max(1, distance);
        double decay = 1.0 / rad;
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        angle = Math.toRadians((angle > 360.0 || angle < 0.0)
                ? MathExtras.remainder(angle, 360.0) : angle);
        float s = (float) NumberTools.sin(angle),
                c = (float) NumberTools.cos(angle);
        double deteriorate = 1.0;
        int dx, dy, width = resistanceMap.length, height = resistanceMap[0].length;
        for (int d = 1; d <= rad; ) {
            dx = startX + Math.round(c * d);
            if(dx < 0 || dx > width)
                break;
            dy = startY + Math.round(s * d);
            if(dy < 0 || dy > height)
                break;
            deteriorate -= decay;
            //check if it's within the lightable area and light if needed
            if (deteriorate > 0.0) {
                light[dx][dy] = Math.min(light[dx][dy] + deteriorate, 1.0);
                if (resistanceMap[dx][dy] >= 1 && deteriorate > decay)
                {
                    startX = dx;
                    startY = dy;
                    d = 1;
                    double flipX = resistanceMap[startX + Math.round(-c * d)][dy],
                            flipY = resistanceMap[dx][startY + Math.round(-s * d)];
                    if(flipX >= 1.0)
                        s = -s;
                    if(flipY >= 1.0)
                        c = -c;
                }
                else ++d;

            }
            else break;
        }

        return light;
    }
    /**
	 * @param width
	 *            The width that {@link #light} should have.
	 * @param height
	 *            The height that {@link #light} should have.
	 */
	private void initializeLightMap(int width, int height) {
		if (light == null)
			light = new double[width][height];
		else {
			if (light.length != width || light[0].length != height)
				/* Size changed */
				light = new double[width][height];
			else {
				/*
				 * Size did not change, we simply need to erase the previous
				 * result
				 */
				ArrayTools.fill(light, 0.0);
			}
		}
	}

	/**
	 * @param width
	 *            The width that {@link #nearLight} should have.
	 * @param height
	 *            The height that {@link #nearLight} should have.
	 */
	private void initializeNearLight(int width, int height) {
		if (nearLight == null)
			nearLight = new boolean[width][height];
		else {
			if (nearLight.length != width || nearLight[0].length != height)
				/* Size changed */
				nearLight = new boolean[width][height];
			else {
				/*
				 * Size did not change, we simply need to erase the previous
				 * result
				 */
				ArrayTools.fill(nearLight, false);
			}
		}
	}

	private static int rippleValue(int type) {
		switch (type) {
		case RIPPLE:
			return 2;
		case RIPPLE_LOOSE:
			return 3;
		case RIPPLE_TIGHT:
			return 1;
		case RIPPLE_VERY_LOOSE:
			return 6;
		default:
			System.err.println("Unrecognized ripple type: " + type + ". Defaulting to RIPPLE");
			return rippleValue(RIPPLE);
		}
	}

    private static void doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect, Radius radiusStrategy) {
        final ArrayDeque<Coord> dq = new ArrayDeque<>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
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

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect, radiusStrategy);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }



    private static void doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect, Radius radiusStrategy, double angle, double span) {
        final ArrayDeque<Coord> dq = new ArrayDeque<>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(Coord.get(x, y));
        while (!dq.isEmpty()) {
            Coord p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (Direction dir : ccw_full) {
                int x2 = p.x + dir.deltaX;
                int y2 = p.y + dir.deltaY;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radiusStrategy.radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }
                double newAngle = NumberTools.atan2_(y2 - starty, x2 - startx);
                if (newAngle > span * 0.5 && newAngle < 1.0 - span * 0.5) 
                    continue;
//if (Math.abs(MathExtras.remainder(angle - newAngle, Math.PI * 2)) > span * 0.5)

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect, radiusStrategy );
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(Coord.get(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private static double nearRippleLight(int x, int y, int rippleNeighbors, int startx, int starty, double decay, double[][] lightMap, double[][] map, boolean[][] indirect, Radius radiusStrategy) {
        if (x == startx && y == starty) {
            return 1;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;
        List<Coord> neighbors = new ArrayList<>();
        double tmpDistance = 0, testDistance;
        Coord c;
        for (Direction di : Direction.OUTWARDS) {
            int x2 = x + di.deltaX;
            int y2 = y + di.deltaY;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                tmpDistance = radiusStrategy.radius(startx, starty, x2, y2);
                int idx = 0;
                for(int i = 0; i < neighbors.size() && i <= rippleNeighbors; i++)
                {
                    c = neighbors.get(i);
                    testDistance = radiusStrategy.radius(startx, starty, c.x, c.y);
                    if(tmpDistance < testDistance) {
                        break;
                    }
                    idx++;
                }
                neighbors.add(idx, Coord.get(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }
        neighbors = neighbors.subList(0, Math.min(neighbors.size(), rippleNeighbors));
/*
        while (neighbors.size() > rippleNeighbors) {
            Coord p = neighbors.remove(0);
            double dist = radiusStrategy.radius(startx, starty, p.x, p.y);
            double dist2 = 0;
            for (Coord p2 : neighbors) {
                dist2 = Math.max(dist2, radiusStrategy.radius(startx, starty, p2.x, p2.y));
            }
            if (dist < dist2) {//not the largest, put it back
                neighbors.add(p);
            }
        }
*/
        double light = 0;
        int lit = 0, indirects = 0;
        for (Coord p : neighbors) {
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

    private static double[][] shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, double[][] lightMap,
                                         double[][] map, Radius radiusStrategy) {
	    return shadowCast(row, start, end, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy, false);
    }

    private static double[][] shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, double[][] lightMap,
                                         double[][] map, Radius radiusStrategy, boolean binary) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    if (binary)
                        lightMap[currentX][currentY] = 1.0;
                    else {
                        double bright = 1 - decay * deltaRadius;
                        lightMap[currentX][currentY] = bright;
                    }
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
                        lightMap = shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }
    private static double[][] shadowCastLimited(int row, double start, double end, int xx, int xy, int yx, int yy,
                                                double radius, int startx, int starty, double decay, double[][] lightMap,
                                                double[][] map, Radius radiusStrategy, double angle, double span) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double deltaRadius = radiusStrategy.radius(deltaX, deltaY),
                        at2 = Math.abs(angle - NumberTools.atan2_(currentY - starty, currentX - startx));// + 1.0) % 1.0;
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius
                        && (at2 <= span * 0.5
                        || at2 >= 1.0 - span * 0.5)) {
                    double bright = 1 - decay * deltaRadius;
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
                        lightMap = shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, radiusStrategy, angle, span);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    private static final double[] directionRanges = new double[8];
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting with the view "pointed at" the given {@code angle} in degrees,
     * extending to different ranges based on the direction the light is traveling.
     * The direction ranges are {@code forward}, {@code sideForward}, {@code side},
     * {@code sideBack}, and {@code back}; all are multiplied by {@code radius}.
     * Assigns to, and returns, a light map where the values represent a percentage of fully
     * lit. The values in light are cleared before this is run, because prior state can make
     * this give incorrect results. You can use {@link #addFOVsInto(double[][], double[][]...)}
     * if you want to mix FOV results, which works as an alternative to using the prior light state.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. If all direction ranges are the same, this acts like
     * {@link #reuseFOV(double[][], double[][], int, int, double, Radius)}; otherwise
     * may produce conical shapes (potentially more than one, or overlapping ones).
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to (roughly); direction ranges will be multiplied by this
     * @param radiusTechnique provides a means to shape the FOV by changing distance calculation (circle, square, etc.)
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param forward the range to extend when the light is within 22.5 degrees of angle; will be interpolated with sideForward
     * @param sideForward the range to extend when the light is between 22.5 and 67.5 degrees of angle; will be interpolated with forward or side
     * @param side the range to extend when the light is between 67.5 and 112.5 degrees of angle; will be interpolated with sideForward or sideBack
     * @param sideBack the range to extend when the light is between 112.5 and 157.5 degrees of angle; will be interpolated with side or back
     * @param back the range to extend when the light is more than 157.5 degrees away from angle; will be interpolated with sideBack
     * @return the computed light grid (the same as {@code light})
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY,
                                      double radius, Radius radiusTechnique, double angle,
                                      double forward, double sideForward, double side, double sideBack, double back) {
        directionRanges[0] = forward * radius;
        directionRanges[7] = directionRanges[1] = sideForward * radius;
        directionRanges[6] = directionRanges[2] = side * radius;
        directionRanges[5] = directionRanges[3] = sideBack * radius;
        directionRanges[4] = back * radius;

        radius = Math.max(1, radius);
        ArrayTools.fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        angle = ((angle >= 360.0 || angle < 0.0)
                ? MathExtras.remainder(angle, 360.0) : angle) * 0.002777777777777778;


        light = shadowCastPersonalized(1, 1.0, 0.0, 0, 1, 1, 0,   radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 1, 0, 0, 1,   radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, -1, 1, 0,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, -1, 0, 0, 1,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, 1, -1, 0,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 1, 0, 0, -1,  radius, startX, startY, light, resistanceMap, radiusTechnique, angle, directionRanges);
        return light;
    }

    private static double[][] shadowCastPersonalized(int row, double start, double end, int xx, int xy, int yx, int yy,
                                                     double radius, int startx, int starty, double[][] lightMap,
                                                     double[][] map, Radius radiusStrategy, double angle, final double[] directionRanges) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double at2 = Math.abs(angle - NumberTools.atan2_(currentY - starty, currentX - startx)) * 8.0,
                        deltaRadius = radiusStrategy.radius(deltaX, deltaY);
                int ia = (int)(at2), low = ia & 7, high = ia + 1 & 7;
                double a = at2 - ia, adjRadius = (1.0 - a) * directionRanges[low] + a * directionRanges[high];
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= adjRadius) {
                    lightMap[currentX][currentY] = 1.0 - (deltaRadius / (adjRadius + 1.0)); // how bright the tile is
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < adjRadius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastPersonalized(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, lightMap, map, radiusStrategy, angle, directionRanges);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    /**
     * Adds an FOV map to another in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Modifies the basis parameter in-place and makes no
     * allocations; this is different from {@link #addFOVs(double[][][])}, which creates a new 2D array.
     * @param basis a 2D double array, which can be empty or returned by calculateFOV() or reuseFOV(); modified!
     * @param addend another 2D double array that will be added into basis; this one will not be modified
     * @return the sum of the 2D double arrays passed, using the dimensions of basis if they don't match
     */
    public static double[][] addFOVsInto(double[][] basis, double[][] addend)
    {
        for (int x = 0; x < basis.length && x < addend.length; x++) {
                for (int y = 0; y < basis[x].length && y < addend[x].length; y++) {
                    basis[x][y] = Math.min(1.0, basis[x][y] + addend[x][y]);
                }
            }
        return basis;
    }
    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Allocates a new 2D double array and returns it.
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(double[][]... maps)
    {
        if(maps == null || maps.length == 0)
            return new double[0][0];
        double[][] map = ArrayTools.copy(maps[0]);
        for(int i = 1; i < maps.length; i++)
        {
            for (int x = 0; x < map.length && x < maps[i].length; x++) {
                for (int y = 0; y < map[x].length && y < maps[i][x].length; y++) {
                    map[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }
    /**
     * Adds multiple FOV maps to basis cell-by-cell, modifying basis; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Returns basis without allocating new objects.
     * @param basis a 2D double array that will be modified by adding values in maps to it and clamping to 1.0 or less 
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return basis, with all elements in all of maps added to the corresponding cells and clamped
     */
    public static double[][] addFOVsInto(double[][] basis, double[][]... maps) {
        if (maps == null || maps.length == 0)
            return basis;
        for (int i = 1; i < maps.length; i++) {
            for (int x = 0; x < basis.length && x < maps[i].length; x++) {
                for (int y = 0; y < basis[x].length && y < maps[i][x].length; y++) {
                    basis[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < basis.length; x++) {
            for (int y = 0; y < basis[x].length; y++) {
                if (basis[x][y] > 1.0) basis[x][y] = 1.0;
            }
        }
        return basis;
    }

    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Allocates a new 2D double array and returns it.
     * @param maps an Iterable of 2D double arrays (most collections implement Iterable),
     *             each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(Iterable<double[][]> maps)
    {
        if(maps == null)
            return new double[0][0];
        Iterator<double[][]> it = maps.iterator();
        if(!it.hasNext())
            return new double[0][0];
        double[][] map = ArrayTools.copy(it.next()), t;
        while (it.hasNext())
        {
            t = it.next();
            for (int x = 0; x < map.length && x < t.length; x++) {
                for (int y = 0; y < map[x].length && y < t[x].length; y++) {
                    map[x][y] += t[x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVs(double[][] losMap, double[][]... maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }

        return map;
    }
    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param basis an existing 2D double array that should have matching width and height to losMap; will be modified
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVsInto(double[][] losMap, double[][] basis, double[][]... maps)

    {
        if(losMap == null || losMap.length <= 0 || losMap[0].length <= 0)
            return addFOVsInto(basis, maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param maps an Iterable of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVs(double[][] losMap, Iterable<double[][]> maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null)
            return map;
        for (double[][] map1 : maps) {
            for (int x = 0; x < width && x < map1.length; x++) {
                for (int y = 0; y < height && y < map1[x].length; y++) {
                    if (losMap[x][y] > 0.0001) {
                        map[x][y] += map1[x][y];
                        if (map[x][y] > 1.0) map[x][y] = 1.0;
                    }
                }
            }
        }
        return map;
    }

    /**
     * Calculates what cells are visible from (startX,startY) using the given resistanceMap; this can be given to
     * mixVisibleFOVs() to limit extra light sources to those visible from the starting point. Just like calling
     * calculateFOV(), this creates a new double[][]; there doesn't appear to be a way to work with Ripple FOV and avoid
     * needing an empty double[][] every time, since it uses previously-placed light to determine how it should spread.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX the center of the LOS map; typically the player's x-position
     * @param startY the center of the LOS map; typically the player's y-position
     * @return an LOS map with the given starting point
     */
    public double[][] calculateLOSMap(double[][] resistanceMap, int startX, int startY)
    {
        if(resistanceMap == null || resistanceMap.length <= 0  || resistanceMap[0].length <= 0)
            return new double[0][0];

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;
        double rad = width + height;
        double decay = 1.0 / rad;

        initializeLightMap(width, height);
        light[startX][startY] = 1;//make the starting space full power

        switch (type) {
            case RIPPLE:
            case RIPPLE_LOOSE:
            case RIPPLE_TIGHT:
            case RIPPLE_VERY_LOOSE:
                initializeNearLight(width, height);
                doRippleFOV(light, rippleValue(type), startX, startY, startX, startY, decay, rad, resistanceMap, nearLight, Radius.SQUARE);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if(light[x][y] > 0.0001)
                            light[x][y] = 1.0;
                    }
                }
                break;
            case SHADOW:
                for (Direction d : Direction.DIAGONALS) {
                    shadowCast(1, 1.0, 0.0, 0, d.deltaX, d.deltaY, 0, rad, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
                    shadowCast(1, 1.0, 0.0, d.deltaX, 0, 0, d.deltaY, rad, startX, startY, decay, light, resistanceMap, Radius.SQUARE, true);
                }
                break;
        }
        return light;
    }
}
