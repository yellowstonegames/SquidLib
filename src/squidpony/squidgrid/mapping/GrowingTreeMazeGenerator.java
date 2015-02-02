package squidpony.squidgrid.mapping;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import squidpony.annotation.Beta;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.RNG;

/**
 *
 *
 * Based in part on code from http://weblog.jamisbuck.org/2011/1/27/maze-generation-growing-tree-algorithm
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class GrowingTreeMazeGenerator {

    private RNG rng = new RNG();
    private int width, height;

    public GrowingTreeMazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Builds and returns a boolean mapping of a maze using the provided chooser method object.
     * 
     * @param choosing the callback object for making the split decision
     * @return 
     */
    public boolean[][] create(ChoosingMethod choosing) {
        boolean[][] map = new boolean[width][height];
        boolean[][] visited = new boolean[width][height];

        int x = rng.nextInt(width / 2);
        int y = rng.nextInt(height / 2);
        x *= 2;
        y *= 2;

        ArrayList<Point> deck = new ArrayList<>();
        deck.add(new Point(x, y));

        List<Direction> dirs = Arrays.asList(Direction.CARDINALS);
        while (!deck.isEmpty()) {
            int i = choosing.chooseIndex(deck.size());
            Point p = deck.get(i);
            Collections.shuffle(dirs, rng.asRandom());

            boolean foundNeighbor = false;
            for (Direction dir : dirs) {
                x = p.x + dir.deltaX * 2;
                y = p.y + dir.deltaY * 2;
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    if (!visited[x][y]) {
                        foundNeighbor = true;
//                        if (rng.nextBoolean()) {
                            visited[x][y] = true;
//                        }
                        map[x][y] = true;
                        map[p.x + dir.deltaX][p.y + dir.deltaY] = true;
                        deck.add(new Point(x, y));
                        break;
                    }
                }
            }

            if (!foundNeighbor) {
                deck.remove(p);
            }
        }

        return map;
    }

    public interface ChoosingMethod {

        /**
         * Given the size to choose from, will return a single value smaller than the passed in value and greater than
         * or equal to 0. The value chosen is dependant on the individual implementation.
         *
         * @param size
         * @return
         */
        public int chooseIndex(int size);
    }
}
