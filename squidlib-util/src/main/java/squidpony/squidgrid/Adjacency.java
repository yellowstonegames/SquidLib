package squidpony.squidgrid;

import squidpony.squidai.DijkstraMap.Measurement;
import squidpony.squidmath.Coord;
import squidpony.squidmath.IntDoubleOrderedMap;

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
    /**
     * Can be changed if the map changes; you should get the neighbors from neighborMaps() again after changing this.
     */
    public int width,
    /**
     * Can be changed if the map changes; you should get the neighbors from neighborMaps() again after changing this.
     */
    height,
    /**
     * Can be changed if the map changes; you should get the neighbors from neighborMaps() again after changing this.
     */
    rotations,
    /**
     * Can be changed if the map changes; you should get the neighbors from neighborMaps() again after changing this.
     */
    depths;

    /**
     * Used in place of a double[][] of costs in CustomDijkstraMap; allows you to set the costs to enter tiles (via
     * {@link #addCostRule(char, double)} or {@link #addCostRule(char, double, boolean)} if the map has rotations).
     * A cost of 1.0 is normal for most implementations; higher costs make a movement harder to perform and take more
     * time if the game uses that mechanic, while lower costs (which should always be greater than 0.0) make a move
     * easier to perform. Most games can do perfectly well with just 1.0 and 2.0, if they use this at all, plus possibly
     * a very high value for impossible moves (say, 9999.0 for something like a submarine trying to enter suburbia).
     * <br>
     * Adjacency implementations are expected to set a reasonable default value for when missing keys are queried, using
     * {@link IntDoubleOrderedMap#defaultReturnValue(double)}; there may be a reason for user code to call this as well.
     */
    public IntDoubleOrderedMap costRules = new IntDoubleOrderedMap(32);

    public abstract int extractX(int data);

    public abstract int extractY(int data);

    public abstract int extractR(int data);

    public abstract int extractN(int data);

    public abstract int composite(int x, int y, int r, int n);

    public abstract boolean validate(int data);

    public Coord extractCoord(int data) {
        return Coord.get(extractX(data), extractY(data));
    }

    public abstract int[][][] neighborMaps();

    public abstract void portal(int[][][] neighbors, int inputPortal, int outputPortal, boolean twoWay);

    public abstract boolean isBlocked(int start, int direction, int[][][] neighbors, double[] map, double wall);

    public IntDoubleOrderedMap addCostRule(char tile, double cost)
    {
        return addCostRule(tile, cost, false);
    }
    public abstract IntDoubleOrderedMap addCostRule(char tile, double cost, boolean isRotation);

    public IntDoubleOrderedMap putAllVariants(IntDoubleOrderedMap map, int key, double value)
    {
        return putAllVariants(map, key, value, 1);
    }
    public abstract IntDoubleOrderedMap putAllVariants(IntDoubleOrderedMap map, int key, double value, int size);

    public int[] invertAdjacent;

    public String show(int data)
    {
        if(data < 0)
            return "(-,-,-)";
        return "(" + extractX(data) + ',' + extractY(data) + ',' + extractR(data) + ')';
    }
    public String showMap(int[] map, int r)
    {
        r %= rotations;
        StringBuilder sb = new StringBuilder(width * height * 8);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(show(map[(y * width + x) * rotations + r])).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

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
            if(metric == Measurement.MANHATTAN)
            {
                directions = Direction.CARDINALS;
                maxAdjacent = 4;
                invertAdjacent = new int[]{1, 0, 3, 2};
            }
            else
            {
                directions = Direction.OUTWARDS;
                maxAdjacent = 8;
                invertAdjacent = new int[]{1, 0, 3, 2, 7, 6, 5, 4};
            }
            twoStepRule = false;
            blockingRule = 2;
            costRules.defaultReturnValue(1.0);
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
        public int[][][] neighborMaps() {
            int[][][] maps = new int[2][maxAdjacent][width * height * rotations * depths];
            for (int m = 0; m < maxAdjacent; m++) {
                Direction dir = directions[m];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        maps[0][m][y * width + x] = composite(x - dir.deltaX, y - dir.deltaY, 0, 0);
                        maps[1][m][y * width + x] = composite(x + dir.deltaX, y + dir.deltaY, 0, 0);
                    }
                }
            }
            return maps;
        }

        @Override
        public boolean isBlocked(int start, int direction, int[][][] neighbors, double[] map, double wall) {
            if(direction < 4)
                return !validate(start);
            int[][] near = neighbors[1];
            switch (direction)
            {
                case 4: //UP_LEFT
                    return (near[0][start] < 0 || map[near[0][start]] >= wall)
                            && (near[2][start] < 0 || map[near[2][start]] >= wall);
                case 5: //UP_RIGHT
                    return (near[0][start] < 0 || map[near[0][start]] >= wall)
                            && (near[3][start] < 0 || map[near[3][start]] >= wall);
                case 6: //DOWN_LEFT
                    return (near[1][start] < 0 || map[near[1][start]] >= wall)
                            && (near[2][start] < 0 || map[near[2][start]] >= wall);
                default: //DOWN_RIGHT
                    return (near[1][start] < 0 || map[near[1][start]] >= wall)
                            && (near[3][start] < 0 || map[near[3][start]] >= wall);
            }
        }

        @Override
        public void portal(int[][][] neighbors, int inputPortal, int outputPortal, boolean twoWay) {
            if(neighbors == null || !validate(inputPortal) || !validate(outputPortal)
                    || neighbors.length != maxAdjacent)
                return;
            for (int d = 0; d < maxAdjacent; d++) {
                for (int i = 0; i < width * height; i++) {
                    if(neighbors[1][d][i] == inputPortal)
                    {
                        neighbors[1][d][i] = outputPortal;
                    }
                    else if(twoWay && neighbors[1][d][i] == outputPortal)
                    {
                        neighbors[1][d][i] = inputPortal;
                    }

                    if(neighbors[0][d][i] == outputPortal)
                    {
                        neighbors[0][d][i] = inputPortal;
                    }
                    else if(twoWay && neighbors[0][d][i] == inputPortal)
                    {
                        neighbors[0][d][i] = outputPortal;
                    }
                }
            }
        }

        @Override
        public IntDoubleOrderedMap addCostRule(char tile, double cost, boolean isRotation) {
            costRules.put(tile, cost);
            return costRules;
        }

        @Override
        public IntDoubleOrderedMap putAllVariants(IntDoubleOrderedMap map, int key, double value, int size) {
            int baseX = key % width, baseY = key / width, comp;
            if (key >= 0 && baseY < height) {
                if (size < 0) {
                    for (int x = size+1; x <= 0; x++) {
                        for (int y = size+1; y <= 0; y++) {
                            comp = composite(baseX + x, baseY + y, 0, 0);
                            if(comp >= 0)
                                map.put(comp, value);
                        }
                    }
                } else {
                    for (int x = 0; x < size; x++) {
                        for (int y = 0; y < size; y++) {
                            comp = composite(baseX + x, baseY + y, 0, 0);
                            if(comp >= 0)
                                map.put(comp, value);
                        }
                    }
                }
            }
            return map;
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
            costRules.defaultReturnValue(0.5);
        }

        @Override
        public IntDoubleOrderedMap addCostRule(char tile, double cost, boolean isRotation) {
            costRules.put(tile, cost * 0.5);
            return costRules;
        }
    }

    public static class RotationAdjacency extends Adjacency implements Serializable {
        private static final long serialVersionUID = 0L;

        private RotationAdjacency() {
            this(20, 20, Measurement.MANHATTAN);
        }
        private int shift;
        public RotationAdjacency(int width, int height, Measurement metric) {
            this.width = width;
            this.height = height;
            measurement = metric;
            if(metric == Measurement.MANHATTAN)
            {
                rotations = 4;
                shift = 2;
                directions = Direction.CARDINALS_CLOCKWISE;
                invertAdjacent = new int[]{2, 3, 0, 1};
            }
            else
            {
                rotations = 8;
                shift = 3;
                directions = Direction.CLOCKWISE;
                invertAdjacent = new int[]{4, 5, 6, 7, 0, 1, 2, 3};
            }
            depths = 1;
            maxAdjacent = 3;
            twoStepRule = false;
            blockingRule = 2;
            costRules.defaultReturnValue(1.0);
            //invertAdjacent = new int[]{2, 1, 0};
        }

        @Override
        public int extractX(int data) {
            return (data >>> shift) % width;
        }

        @Override
        public int extractY(int data) {
            return (data >>> shift) / width;
        }

        @Override
        public int extractR(int data) {
            return data & (rotations - 1);
        }

        @Override
        public int extractN(int data) {
            return 0;
        }

        @Override
        public int composite(int x, int y, int r, int n) {
            if(x < 0 || y < 0 || x >= width || y >= height || r < 0 || r >= rotations)
                return -1;
            return ((y * width + x) << shift) | r;
        }

        @Override
        public boolean validate(int data) {
            return data >= 0 && extractY(data) < height;
        }

        @Override
        public int[][][] neighborMaps() {
            int[][][] maps = new int[2][maxAdjacent][width * height * rotations * depths];
            int current;
            Direction dir;
            for (int r = 0; r < rotations; r++) {
                dir = directions[r];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        current = ((y * width + x) << shift) | r;
                        maps[0][1][current] = composite(x - dir.deltaX, y - dir.deltaY, r, 0);
                        maps[1][1][current] = composite(x + dir.deltaX, y + dir.deltaY, r, 0);
                        maps[0][0][current] = maps[1][0][current] = composite(x, y, r - 1 & (rotations - 1), 0);
                        maps[0][2][current] = maps[1][2][current] = composite(x, y, r + 1 & (rotations - 1), 0);
                        //maps[0][composite(x, y, r - 1 & (rotations - 1), 0)] = current;
                        //maps[2][composite(x, y, r + 1 & (rotations - 1), 0)] = current;
                    }
                }
            }
            return maps;
        }

        @Override
        public boolean isBlocked(int start, int direction, int[][][] neighbors, double[] map, double wall) {
            if(rotations <= 4 || (direction & 1) == 0)
                return !validate(start);

            return neighbors[1][0][start] < 0 || map[neighbors[1][0][start]] >= wall
                    || neighbors[1][2][start] < 0 || map[neighbors[1][2][start]] >= wall;
        }

        @Override
        public void portal(int[][][] neighbors, int inputPortal, int outputPortal, boolean twoWay) {
            if(neighbors == null || !validate(inputPortal) || !validate(outputPortal)
                    || neighbors.length != maxAdjacent)
                return;
            for (int i = 0; i < width * height * rotations; i++) {
                if (neighbors[0][1][i] == inputPortal) {
                    neighbors[0][1][i] = outputPortal;
                } else if (twoWay && neighbors[0][1][i] == outputPortal) {
                    neighbors[0][1][i] = inputPortal;
                }

                if (neighbors[1][1][i] == outputPortal) {
                    neighbors[1][1][i] = inputPortal;
                } else if (twoWay && neighbors[1][1][i] == inputPortal) {
                    neighbors[1][1][i] = outputPortal;
                }
            }
        }

        @Override
        public IntDoubleOrderedMap addCostRule(char tile, double cost, boolean isRotation) {
            if(isRotation)
                costRules.put(tile | 0x10000, Math.max(0.001, cost));
            else
                costRules.put(tile, cost);
            return costRules;
        }
        @Override
        public IntDoubleOrderedMap putAllVariants(IntDoubleOrderedMap map, int key, double value, int size) {
            int baseX = (key >>> shift) % width, baseY = (key>>>shift) / width, comp;
            if (key >= 0 && baseY < height) {
                if(size == 1)
                {
                    for (int r = 0; r < rotations; r++) {
                        comp = composite(baseX, baseY, r, 0);
                        if(comp >= 0)
                            map.put(comp, value);
                    }
                }
                else if (size < 0) {
                    for (int x = size+1; x <= 0; x++) {
                        for (int y = size+1; y <= 0; y++) {
                            for (int r = 0; r < rotations; r++) {
                                comp = composite(baseX + x, baseY + y, r, 0);
                                if(comp >= 0)
                                    map.put(comp, value);
                            }
                        }
                    }
                } else {
                    for (int x = 0; x < size; x++) {
                        for (int y = 0; y < size; y++) {
                            for (int r = 0; r < rotations; r++) {
                                comp = composite(baseX + x, baseY + y, r, 0);
                                if(comp >= 0)
                                    map.put(comp, value);
                            }
                        }
                    }
                }
            }
            return map;
        }
    }
}