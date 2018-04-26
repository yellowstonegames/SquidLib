package squidpony.squidgrid;

import squidpony.squidmath.*;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Line of Sight (LOS) algorithms find if there is or is not a path between two
 * given points.
 * <br>
 * The line found between two points will end at either the target, the
 * obstruction closest to the start, or the edge of the map.
 * <br>
 * For normal line of sight usage, you should prefer Bresenham lines, and these
 * are the default (they can also be specified by passing {@link #BRESENHAM} to
 * the constructor). For more specialized usage, there are other kinds of LOS in
 * this class, like lines that make no diagonal moves between cells (using
 * {@link #ORTHO}, or lines that check a wide path (but these use different
 * methods, like {@link #thickReachable(Radius)}).
 * <br>
 * Performance-wise, all of these methods are rather fast and about the same speed.
 * {@link #RAY} is a tiny fraction faster than {@link #BRESENHAM} but produces
 * rather low-quality lines in comparison. Calculating the visibility of 40,000
 * lines in a 102x102 dungeon takes within 3% of 950ms (on an Intel i7-4700MQ laptop
 * processor) for every one of BRESENHAM, DDA, ORTHO, and RAY, even with ORTHO
 * finding a different kind of line by design.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 * @author Tommy Ettinger Added DDA, ORTHO, and the thick lines; some cleanup
 * @author smelC optimized several methods
 */
public class LOS implements Serializable {
    private static final long serialVersionUID = 1L;
    //constants to indicate desired type of solving algorithm to use
    /**
     * A Bresenham-based line-of-sight algorithm.
     */
    public static final int BRESENHAM = 1;
    /**
     * Uses Wu's Algorithm as modified by Elias to draw the line. Does
     * not end at an obstruction but rather returns one of the possible
     * attempted paths in full.
     */
    public static final int ELIAS = 2;
    /**
     * Uses a series of rays internal to the start and end point to
     * determine visibility. Appearance is extremely close to DDA, which
     * is also probably a faster algorithm, so BRESENHAM (which can look
     * a little better) and DDA are recommended instead of RAY.
     */
    public static final int RAY = 3;
    /**
     * Draws a line using only North/South/East/West movement.
     */
    public static final int ORTHO = 4;
    /**
     * Optimized algorithm for Bresenham-like lines. There are slight
     * differences in many parts of the lines this draws when compared
     * to Bresenham lines, but it may also perform significantly better,
     * and may also be useful as a building block for more complex LOS.
     * Virtually identical in results to RAY, and just a hair slower, but
     * better-tested and more predictable.
     */
    public static final int DDA = 5;
    /**
     * Draws a line as if with a thick brush, going from a point between
     * a corner of the starting cell and the center of the starting cell
     * to the corresponding corner of the target cell, and considers the
     * target visible if any portion of the thick stroke reached it. Will
     * result in 1-width lines for exactly-orthogonal or exactly-diagonal
     * lines and some parts of other lines, but usually is 2 cells wide.
     */
    public static final int THICK = 6;
    private ArrayDeque<Coord> lastPath = new ArrayDeque<>();
    private int type;
    private double[][] resistanceMap;
    private int startx, starty, targetx, targety;
    private Elias elias = null;
    private LOS los1 = null, los2 = null;
    /**
     * Gets the radius strategy this uses.
     * @return the current Radius enum used to measure distance; starts at CIRCLE if not specified
     */
    public Radius getRadiusStrategy() {
        return radiusStrategy;
    }

    /**
     * Set the radius strategy to the given Radius; the default is CIRCLE if this is not called.
     * @param radiusStrategy a Radius enum to determine how distances are measured
     */
    public void setRadiusStrategy(Radius radiusStrategy) {
        this.radiusStrategy = radiusStrategy;
    }

    private Radius radiusStrategy = Radius.CIRCLE;

    /**
     * Constructs an LOS that will draw Bresenham lines and measure distances using the CIRCLE radius strategy.
     */
    public LOS() {
        this(BRESENHAM);
    }

    /**
     * Constructs an LOS with the given type number, which must equal a static field in this class such as BRESENHAM.
     * @param type an int that must correspond to the value of a static field in this class (such as BRESENHAM)
     */
    public LOS(int type) {
        this.type = type;
        if(type == ELIAS)
        {
            elias = new Elias();
            los1 = new LOS(BRESENHAM);
            los2 = new LOS(BRESENHAM);

        }
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with setRadiusStrategy .
     *
     * @param walls '#' is fully opaque, anything else is fully transparent, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(char[][] walls, int startx, int starty, int targetx, int targety) {
        if(walls.length < 1) return false;
        double[][] resMap = new double[walls.length][walls[0].length];
        for(int x = 0; x < walls.length; x++)
        {
            for(int y = 0; y < walls[0].length; y++)
            {
                resMap[x][y] = (walls[x][y] == '#') ? 1.0 : 0.0;
            }
        }
        return isReachable(resMap, startx, starty, targetx, targety, radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * Does not take into account resistance less than opaque or distance cost.
     *
     * Uses RadiusStrategy.CIRCLE, or whatever RadiusStrategy was set with setRadiusStrategy .
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(double[][] resistanceMap, int startx, int starty, int targetx, int targety) {
        return isReachable(resistanceMap, startx, starty, targetx, targety, radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(double[][] resistanceMap, int startx, int starty, int targetx, int targety, Radius radiusStrategy) {
        if(resistanceMap.length < 1) return false;
        this.resistanceMap = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.targetx = targetx;
        this.targety = targety;
        switch (type) {
            case BRESENHAM:
                return bresenhamReachable(radiusStrategy);
            case ELIAS:
            	return eliasReachable(radiusStrategy);
            case RAY:
                return rayReachable(radiusStrategy);
            case ORTHO:
                return orthoReachable(radiusStrategy);
            case DDA:
                return ddaReachable(radiusStrategy);
            case THICK:
                return thickReachable(radiusStrategy);
        }
        return false;
    }

    /**
     * Returns true if a line can be drawn from the start point to the target
     * point without intervening obstructions.
     *
     * @param walls '#' is fully opaque, anything else is fully transparent, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean isReachable(char[][] walls, int startx, int starty, int targetx, int targety, Radius radiusStrategy) {
        if(walls.length < 1) return false;
        double[][] resMap = new double[walls.length][walls[0].length];
        for(int x = 0; x < walls.length; x++)
        {
            for(int y = 0; y < walls[0].length; y++)
            {
                resMap[x][y] = (walls[x][y] == '#') ? 1.0 : 0.0;
            }
        }
        return isReachable(resMap, startx, starty, targetx, targety, radiusStrategy);
    }

    /**
     * Returns true if a line can be drawn from the any of the points within spread cells of the start point,
     * to any of the corresponding points at the same direction and distance from the target point, without
     * intervening obstructions. Primarily useful to paint a broad line that can be retrieved with getLastPath.
     *
     * @param walls '#' is fully opaque, anything else is fully transparent, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @param spread the number of cells outward, measured by radiusStrategy, to place extra start and target points
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean spreadReachable(char[][] walls, int startx, int starty, int targetx, int targety, Radius radiusStrategy, int spread) {
        if(walls.length < 1) return false;
        resistanceMap = new double[walls.length][walls[0].length];
        for(int x = 0; x < walls.length; x++)
        {
            for(int y = 0; y < walls[0].length; y++)
            {
                resistanceMap[x][y] = (walls[x][y] == '#') ? 1.0 : 0.0;
            }
        }
        this.startx = startx;
        this.starty = starty;
        this.targetx = targetx;
        this.targety = targety;

        return brushReachable(radiusStrategy, spread);
    }
    /**
     * Returns true if a line can be drawn from the any of the points within spread cells of the start point,
     * to any of the corresponding points at the same direction and distance from the target point, without
     * intervening obstructions. Primarily useful to paint a broad line that can be retrieved with getLastPath.
     *
     * @param resistanceMap 0.0 is fully transparent, 1.0 is fully opaque, as always this uses x,y indexing.
     * @param startx starting x position on the grid
     * @param starty starting y position on the grid
     * @param targetx ending x position on the grid
     * @param targety ending y position on the grid
     * @param radiusStrategy the strategy to use in computing unit distance
     * @param spread the number of cells outward, measured by radiusStrategy, to place extra start and target points
     * @return true if a line can be drawn without being obstructed, false otherwise
     */
    public boolean spreadReachable(double[][] resistanceMap, int startx, int starty, int targetx, int targety, Radius radiusStrategy, int spread) {
        if(resistanceMap.length < 1) return false;
        this.resistanceMap = resistanceMap;
        this.startx = startx;
        this.starty = starty;
        this.targetx = targetx;
        this.targety = targety;

        return brushReachable(radiusStrategy, spread);
    }
    /**
     * Returns the path of the last LOS calculation, with the starting point as
     * the head of the queue.
     *
     * @return the last path found during LOS calculation, as a ArrayDeque of Coord with the starting point at the head
     */
    public ArrayDeque<Coord> getLastPath() {
        return lastPath;
    }
/*
    private boolean bresenhamReachable(Radius radiusStrategy) {
        Queue<Coord> path = Bresenham.line2D(startx, starty, targetx, targety);
        lastPath = new ArrayDeque<>();
        lastPath.add(Coord.get(startx, starty));
        double decay = 1 / radiusStrategy.radius(startx, starty, targetx, targety);
        double currentForce = 1;
        for (Coord p : path) {
            lastPath.offer(p);
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end 
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y];
            }
            double r = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (r * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }
*/
    private boolean bresenhamReachable(Radius radiusStrategy) {
        Coord[] path = Bresenham.line2D_(startx, starty, targetx, targety);
        lastPath = new ArrayDeque<>();
        double rad = radiusStrategy.radius(startx, starty, targetx, targety);
        if(rad == 0.0) {
            lastPath.add(Coord.get(startx, starty));
            return true; // already at the point; we can see our own feet just fine!
        }
        double decay = 1 / rad;
        double currentForce = 1;
        Coord p;
        for (int i = 0; i < path.length; i++) {
            p = path[i];
            lastPath.offer(p);
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y];
            }
            double r = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (r * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }

    private boolean orthoReachable(Radius radiusStrategy) {
        Coord[] path = OrthoLine.line_(startx, starty, targetx, targety);
        lastPath = new ArrayDeque<>();
        double rad = radiusStrategy.radius(startx, starty, targetx, targety);
        if(rad == 0.0) {
            lastPath.add(Coord.get(startx, starty));
            return true; // already at the point; we can see our own feet just fine!
        }
        double decay = 1 / rad;
        double currentForce = 1;
        Coord p;
        for (int i = 0; i < path.length; i++) {
            p = path[i];
            lastPath.offer(p);
            if (p.x == targetx && p.y == targety) {
                return true;//reached the end
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y];
            }
            double r = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (r * decay) <= 0) {
                return false;//too much resistance
            }
        }
        return false;//never got to the target point
    }

    private boolean ddaReachable(Radius radiusStrategy) {
        Coord[] path = DDALine.line_(startx, starty, targetx, targety);
        lastPath = new ArrayDeque<>();
        double rad = radiusStrategy.radius(startx, starty, targetx, targety);
        if(rad == 0.0) {
            lastPath.add(Coord.get(startx, starty));
            return true; // already at the point; we can see our own feet just fine!
        }
        double decay = 1 / rad;
        double currentForce = 1;
        Coord p;
        for (int i = 0; i < path.length; i++) {
            p = path[i];
            if (p.x == targetx && p.y == targety) {
                lastPath.offer(p);
                return true;//reached the end
            }
            if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[p.x][p.y];
            }
            double r = radiusStrategy.radius(startx, starty, p.x, p.y);
            if (currentForce - (r * decay) <= 0) {
                return false;//too much resistance
            }
            lastPath.offer(p);
        }
        return false;//never got to the target point
    }

    private boolean thickReachable(Radius radiusStrategy) {
        lastPath = new ArrayDeque<>();
        double dist = radiusStrategy.radius(startx, starty, targetx, targety);
        double decay = 1.0 / dist; // note: decay can be positive infinity if dist is 0; this is actually OK
        OrderedSet<Coord> visited = new OrderedSet<>((int) dist + 3);
        List<List<Coord>> paths = new ArrayList<>(4);
        /* // actual corners
        paths.add(DDALine.line(startx, starty, targetx, targety, 0, 0));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0, 0xffff));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0xffff, 0));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0xffff, 0xffff));
        */
        // halfway between the center and a corner
        paths.add(DDALine.line(startx, starty, targetx, targety, 0x3fff, 0x3fff));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0x3fff, 0xbfff));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0xbfff, 0x3fff));
        paths.add(DDALine.line(startx, starty, targetx, targety, 0xbfff, 0xbfff));

        int length = Math.max(paths.get(0).size(), Math.max(paths.get(1).size(),
                Math.max(paths.get(2).size(), paths.get(3).size())));
        double[] forces = new double[]{1,1,1,1};
        boolean[] go = new boolean[]{true, true, true, true};
        Coord p;
        for (int d = 0; d < length; d++) {
            for (int pc = 0; pc < 4; pc++) {
                List<Coord> path = paths.get(pc);
                if(d < path.size() && go[pc])
                    p = path.get(d);
                else continue;
                if (p.x == targetx && p.y == targety) {
                    visited.add(p);
                    lastPath.addAll(visited);
                    return true;//reached the end
                }
                if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                    forces[pc] -= resistanceMap[p.x][p.y];
                }
                double r = radiusStrategy.radius(startx, starty, p.x, p.y);
                if (forces[pc] - (r * decay) <= 0) {
                    go[pc] = false;
                    continue;//too much resistance
                }
                visited.add(p);
            }
        }
        lastPath.addAll(visited);
        return false;//never got to the target point
    }

    private boolean brushReachable(Radius radiusStrategy, int spread) {
        lastPath = new ArrayDeque<>();
        double dist = radiusStrategy.radius(startx, starty, targetx, targety) + spread * 2, decay = 1 / dist;
        OrderedSet<Coord> visited = new OrderedSet<>((int) (dist + 3) * spread);
        List<List<Coord>> paths = new ArrayList<>((int) (radiusStrategy.volume2D(spread) * 1.25));
        int length = 0;
        List<Coord> currentPath;
        for (int i = -spread; i <= spread; i++) {
            for (int j = -spread; j <= spread; j++) {
                if(radiusStrategy.inRange(startx, starty, startx + i, starty + j, 0, spread)
                        && startx + i >= 0 && starty + j >= 0
                        && startx + i < resistanceMap.length && starty + j < resistanceMap[0].length
                        && targetx + i >= 0 && targety + j >= 0
                        && targetx + i < resistanceMap.length && targety + j < resistanceMap[0].length) {
                    for (int q = 0x3fff; q < 0xffff; q += 0x8000) {
                        for (int r = 0x3fff; r < 0xffff; r += 0x8000) {
                            currentPath = DDALine.line(startx+i, starty+j, targetx+i, targety+j, q, r);
                            paths.add(currentPath);
                            length = Math.max(length, currentPath.size());
                        }
                    }
                }
            }
        }
        double[] forces = new double[paths.size()];
        Arrays.fill(forces, 1.0);
        boolean[] go = new boolean[paths.size()];
        Arrays.fill(go, true);
        Coord p;
        boolean found = false;
        for (int d = 0; d < length; d++) {
            for (int pc = 0; pc < paths.size(); pc++) {
                List<Coord> path = paths.get(pc);
                if(d < path.size() && go[pc])
                    p = path.get(d);
                else continue;
                if (p.x == targetx && p.y == targety) {
                    found = true;
                }
                if (p.x != startx || p.y != starty) {//don't discount the start location even if on resistant cell
                    forces[pc] -= resistanceMap[p.x][p.y];
                }
                double r = radiusStrategy.radius(startx, starty, p.x, p.y);
                if (forces[pc] - (r * decay) <= 0) {
                    go[pc] = false;
                    continue;//too much resistance
                }
                visited.add(p);
            }
        }
        lastPath.addAll(visited);
        return found;//never got to the target point
    }

    private boolean rayReachable(Radius radiusStrategy) {
        lastPath = new ArrayDeque<>();//save path for later retrieval
        if (startx == targetx && starty == targety) {//already there!
            lastPath.add(Coord.get(startx, starty));
            return true;
        }

        int width = resistanceMap.length;
        int height = resistanceMap[0].length;

        Coord end = Coord.get(targetx, targety);
        //find out which direction to step, on each axis
        int stepX = targetx == startx ? 0 : (targetx - startx >> 31 | 1), // signum with less converting to/from float
                stepY = targety == starty ? 0 : (targety - starty >> 31 | 1);

        int deltaY = Math.abs(targetx - startx),
                deltaX = Math.abs(targety - starty);

        int testX = startx,
                testY = starty;

        int maxX = deltaX,
                maxY = deltaY;

        while (testX >= 0 && testX < width && testY >= 0 && testY < height && (testX != targetx || testY != targety)) {
            lastPath.add(Coord.get(testX, testY));
            if (maxY - maxX > deltaX) {
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    end = Coord.get(testX, testY);
                    break;
                }
            } else if (maxX - maxY > deltaY) {
                maxY += deltaY;
                testY += stepY;
                if (resistanceMap[testX][testY] >= 1f) {
                    end = Coord.get(testX, testY);
                    break;
                }
            } else {//directly on diagonal, move both full step
                maxY += deltaY;
                testY += stepY;
                maxX += deltaX;
                testX += stepX;
                if (resistanceMap[testX][testY] >= 1f) {
                    end = Coord.get(testX, testY);
                    break;
                }
            }
            if (radiusStrategy.radius(testX, testY, startx, starty) > radiusStrategy.radius(startx, starty, end.x, end.y)) {//went too far
                break;
            }
        }

        if (end.x >= 0 && end.x < width && end.y >= 0 && end.y < height) {
            lastPath.add(Coord.get(end.x, end.y));
        }

        return end.x == targetx && end.y == targety;
    }

    private boolean eliasReachable(Radius radiusStrategy) {
        if(elias == null)
        {
            elias = new Elias();
            los1 = new LOS(BRESENHAM);
            los2 = new LOS(BRESENHAM);
        }
        List<Coord> ePath = elias.line(startx, starty, targetx, targety);
        
        for(Coord p : ePath)
        {
            //if a non-solid midpoint on the path can see both the start and end, consider the two ends to be able to see each other
            if (resistanceMap[p.x][p.y] < 1
                    && radiusStrategy.radius(startx, starty, p.x, p.y) <= radiusStrategy.radius(startx, starty, targetx, targety)
                    && los1.isReachable(resistanceMap, p.x, p.y, targetx, targety, radiusStrategy)
                    && los2.isReachable(resistanceMap, startx, starty, p.x, p.y, radiusStrategy)) {

                //record actual sight path used
                lastPath.clear();
                lastPath.addAll(los2.lastPath);
                lastPath.addAll(los1.lastPath);
                return true;
            }

        }
//        for (Coord p : ePath) {
//            EliasWorker worker = new EliasWorker(p.x, p.y, radiusStrategy);
//            Thread thread = new Thread(worker);
//            thread.start();
//            pool.put(worker, thread);
//        }
//
//        for (EliasWorker w : pool.keySet()) {
//            try {
//                pool.get(w).join();
//            } catch (InterruptedException ex) {
//            }
//            if (w.succeeded) {
//                lastPath = w.path;
//                return true;
//            }
//        }

        return false;//never got to the target point
    }
}
