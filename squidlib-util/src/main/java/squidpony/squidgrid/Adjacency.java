package squidpony.squidgrid;

import squidpony.squidai.DijkstraMap.Measurement;
import squidpony.squidmath.Coord;

import java.io.Serializable;

/**
 * Some classes need detailed information about what cells are considered adjacent to other cells, and may
 * need to construct a customized mapping of cells to their neighbors. Implementations of this abstract
 * class provide information about all sorts of things, including the distance metric (from DijkstraMap),
 * but also the maximum number of states that can be moved to in one step (including rotations at the same
 * point in space, in some cases), and whether the type of map uses a "two-step" rule that needs two
 * sequential moves in the same direction to be viable and unobstructed to allow movement (which is
 * important in thin-wall maps).
 * <br>
 * When CustomDijkstraMap and similar classes need to store more information about a point than just its
 * (x,y) position, they also use implementations of this class to cram more information in a single int.
 * This abstract class provides methods to obtain four different numbers from a single int, though not all
 * implementations may provide all four as viable options. It also provides a utility to get a Coord from an
 * int. X and Y are exactly what they always mean in 2D Coords, R is typically used for rotation, and N is
 * typically used for anything else when it is present. The convention is to use N for the Z-axis when
 * elevation/depth should be tracked, or for any more specialized extensions to the information carried at
 * a point. The composite() method produces a compressed int from X, Y, R, and N values, and the validate()
 * method allows code to quickly check if an int is valid data this class can use. Other information is
 * tracked by fields, such as height, width, rotations, and depths, where the maximum number of possible
 * states is given by height * width * rotations * depths, and the minimum for any of these int fields is 1.
 * <br>
 * Lastly, the neighborMaps() method produces very important information about what neighbors each cell has,
 * and by modifying the returned int[][], you can produce "portal" effects, wraparound, and other useful
 * concepts. The value it returns consists of an array (with length == maxAdjacent) of arrays (each with the
 * same size, length == width * height * rotations * depth). The values in the inner arrays can be any int
 * between 0 and (width * height * rotations * depth), which refers to the index in any of the inner arrays of
 * a neighboring cell, or can be -1 if there is no neighbor possible here (typically at edges or corners of the
 * map, some of the neighbors are not valid and so use -1). In normal usage, a for loop is used from 0 to
 * maxAdjacent, and in each iteration the same index is looked up (the current cell, encoded as by composite()
 * or obtained as an already-composited neighbor earlier), and this normally gets a different neighbor every
 * time. In methods that do a full-map search or act in a way that can possibly loop back over an existing cell
 * in the presence of wrapping (toroidal or "modulus" maps) or portals, you may want to consider tracking a
 * count of how many cells have been processed and terminate any processing of further cells if the count
 * significantly exceeds the number of cells on the map (terminating when 4 times the cell count is reached may
 * be the most extreme case for very-portal-heavy maps).
 * Created by Tommy Ettinger on 8/12/2016.
 */
public abstract class Adjacency implements Serializable {
    private static final long serialVersionUID = 0L;
    /**
     * The array of all possible directions this allows, regardless of cost.
     */
    public Direction[] directions;
    /**
     * The maximum number of states that can be considered adjacent; when rotations are present and have a
     * cost this is almost always 3 (move forward, turn left, turn right), and in most other cases this is
     * 4 (when using Manhattan distance) or 8 (for other distance metrics).
     */
    public int maxAdjacent;
    /**
     * Only needed for thin-wall maps; this requires two steps in the same direction to both be valid moves
     * for that direction to be considered, and always moves the pathfinder two steps, typically to cells
     * with even numbers for both x and y (where odd-number-position cells are used for edges or corners
     * between cells, and can still be obstacles or possible to pass through, but not stay on).
     */
    public boolean twoStepRule;
    /**
     * If you want obstacles present in orthogonal cells to prevent pathfinding along the diagonal between them, this
     * can be used to make single-cell diagonal walls non-viable to move through, or even to prevent diagonal movement if any
     * one obstacle is orthogonally adjacent to both the start and target cell of a diagonal move.
     * <br>
     * If this is 0, as a special case no orthogonal obstacles will block diagonal moves.
     * <br>
     * If this is 1, having one orthogonal obstacle adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. This generally blocks movement around corners, the "hard
     * corner" rule used in some games.
     * <br>
     * If this is 2, having two orthogonal obstacles adjacent to both the current cell and the cell the pathfinder is
     * trying to diagonally enter will block diagonal moves. As an example, if there is a wall to the north and a wall
     * to the east, then the pathfinder won't be able to move northeast even if there is a floor there.
     * <br>
     * A similar effect can be achieved with a little more control by using thin walls, where the presence of
     * a "thin corner" can block diagonal movement through that corner, or the absence of a blocking wall in
     * a corner space allows movement through it.
     */
    public int blockingRule;
    /**
     * This affects how distance is measured on diagonal directions vs. orthogonal directions. MANHATTAN should form a
     * diamond shape on a featureless map, while CHEBYSHEV and EUCLIDEAN will form a square. EUCLIDEAN does not affect
     * the length of paths, though it will change the DijkstraMap's gradientMap to have many non-integer values, and
     * that in turn will make paths this finds much more realistic and smooth (favoring orthogonal directions unless a
     * diagonal one is a better option).
     */
    public Measurement measurement;
    public int width, height, rotations, depths;

    public abstract int extractX(int data);

    public abstract int extractY(int data);

    public abstract int extractR(int data);

    public abstract int extractN(int data);

    public abstract int composite(int x, int y, int r, int n);

    public abstract boolean validate(int data);

    public Coord extractCoord(int data) {
        return Coord.get(extractX(data), extractY(data));
    }

    public abstract int[][] neighborMaps();

    public abstract boolean isBlocked(int start, int direction, int[][] neighbors, double[] map, double wall);

    public static class BasicAdjacency extends Adjacency implements Serializable {
        private static final long serialVersionUID = 0L;

        private BasicAdjacency() {
            this(20, 20, Measurement.MANHATTAN);
        }

        public BasicAdjacency(int width, int height, Measurement metric) {
            this.width = width;
            this.height = height;
            rotations = 1;
            depths = 1;
            measurement = metric;
            directions = (measurement == Measurement.MANHATTAN) ? Direction.CARDINALS : Direction.OUTWARDS;
            maxAdjacent = (measurement == Measurement.MANHATTAN) ? 4 : 8;
            twoStepRule = false;
            blockingRule = 2;
        }

        @Override
        public int extractX(int data) {
            return data % width;
        }

        @Override
        public int extractY(int data) {
            return data / width;
        }

        @Override
        public int extractR(int data) {
            return 0;
        }

        @Override
        public int extractN(int data) {
            return 0;
        }

        @Override
        public int composite(int x, int y, int r, int n) {
            if(x < 0 || y < 0 || x >= width || y >= height)
                return -1;
            return y * width + x;
        }

        @Override
        public boolean validate(int data) {
            return data >= 0 && extractY(data) < height;
        }

        @Override
        public int[][] neighborMaps() {
            int[][] maps = new int[maxAdjacent][width * height * rotations * depths];
            for (int m = 0; m < maxAdjacent; m++) {
                Direction dir = directions[m];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        maps[m][y * width + x] = composite(x + dir.deltaX, y + dir.deltaY, 0, 0);
                    }
                }
            }
            return maps;
        }

        @Override
        public boolean isBlocked(int start, int direction, int[][] neighbors, double[] map, double wall) {
            if(direction < 4 || !validate(start))
                return false;
            switch (direction)
            {
                case 4: //UP_LEFT
                    return (neighbors[0][start] < 0 || map[neighbors[0][start]] >= wall)
                            && (neighbors[2][start] < 0 || map[neighbors[2][start]] >= wall);
                case 5: //UP_RIGHT
                    return (neighbors[0][start] < 0 || map[neighbors[0][start]] >= wall)
                            && (neighbors[3][start] < 0 || map[neighbors[3][start]] >= wall);
                case 6: //DOWN_LEFT
                    return (neighbors[1][start] < 0 || map[neighbors[1][start]] >= wall)
                            && (neighbors[2][start] < 0 || map[neighbors[2][start]] >= wall);
                default: //DOWN_RIGHT
                    return (neighbors[1][start] < 0 || map[neighbors[1][start]] >= wall)
                            && (neighbors[3][start] < 0 || map[neighbors[3][start]] >= wall);
            }
        }
    }

    public static class ThinWallAdjacency extends BasicAdjacency implements Serializable {
        private static final long serialVersionUID = 0L;

        private ThinWallAdjacency() {
            this(20, 20, Measurement.MANHATTAN);
        }

        public ThinWallAdjacency(int width, int height, Measurement metric) {
            super(width, height, metric);
            twoStepRule = true;
        }
    }

}