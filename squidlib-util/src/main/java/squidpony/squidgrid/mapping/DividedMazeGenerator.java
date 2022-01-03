package squidpony.squidgrid.mapping;

import squidpony.ArrayTools;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.IntVLA;

import java.util.ArrayDeque;

/**
 * Recursively divided maze. Creates only walls and passages, as the chars {@code '#'} and {@code '.'}. You may get
 * better mazes from using {@link GrowingTreeMazeGenerator}; this generator produces lots of narrow dead-end hallways.
 * <p>
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class DividedMazeGenerator implements IDungeonGenerator {

    private static class DividedMazeRoom {

        private final int left, top, right, bottom;

        public DividedMazeRoom(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    private int width, height;
    private char[][] map;
    private IRNG rng;

    /**
     * Sets up the generator to make mazes the given width and height. The mazes
     * have a solid wall border.
     *
     * @param width
     * @param height
     */
    public DividedMazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        rng = new GWTRNG();
    }

    /**
     * Sets up the generator to make mazes the given width and height. The mazes
     * have a solid wall border.
     *
     * @param width  in cells
     * @param height in cells
     * @param rng    the random number generator to use
     */
    public DividedMazeGenerator(int width, int height, IRNG rng) {
        this.width = width;
        this.height = height;
        this.rng = rng;
    }

    /**
     * Builds a maze. As usual, {@code '#'} represents a wall, and {@code '.'} represents a floor.
     *
     * @return
     */
    public char[][] generate() {
        map = ArrayTools.fill('.', width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(x == 0 || y == 0 || x + 1 == width || y + 1 == height)
                    map[x][y] = '#';
            }
        }

        process();

        return map;
    }

    /**
     * Gets the most recently-produced dungeon as a 2D char array, usually produced by calling {@link #generate()}. This
     * may reuturn null if generate() has not been called. This passes a direct reference and not a copy,
     * so you can normally modify the returned array to propagate changes back into this IDungeonGenerator.
     *
     * @return the most recently-produced dungeon/map as a 2D char array
     */
    @Override
    public char[][] getDungeon() {
        return map;
    }

    private void process() {
        ArrayDeque<DividedMazeRoom> stack = new ArrayDeque<>();
        stack.offer(new DividedMazeRoom(1, 1, width - 2, height - 2));
        IntVLA availX = new IntVLA(), availY = new IntVLA();
        Direction[] dirs = new Direction[4];
        System.arraycopy(Direction.CARDINALS, 0, dirs, 0, 4);
        while (!stack.isEmpty()) {
            DividedMazeRoom room = stack.removeFirst();
            availX.clear();
            availY.clear();

            for (int x = room.left + 1; x < room.right; x++) {
                boolean top = '#' == map[x][room.top - 1];
                boolean bottom = '#' == map[x][room.bottom + 1];
                if (top && bottom && (x & 1) == 0) {
                    availX.add(x);
                }
            }

            for (int y = room.top + 1; y < room.bottom; y++) {
                boolean left = '#' == map[room.left - 1][y];
                boolean right = '#' == map[room.right + 1][y];
                if (left && right && (y & 1) == 0) {
                    availY.add(y);
                }
            }

            if (availX.isEmpty() || availY.isEmpty()) {
                continue;
            }

            int x2 = availX.getRandomElement(rng);
            int y2 = availY.getRandomElement(rng);

            map[x2][y2] = '#';

            for (int x = room.left; x < x2; x++) {
                map[x][y2] = '#';
            }             
            for (int x = x2 + 1; x <= room.right; x++) {
                map[x][y2] = '#'; 
            }
            for (int y = room.top; y < y2; y++) {
                map[x2][y] = '#'; 
            }
            for (int y = y2 + 1; y <= room.bottom; y++) {
                map[x2][y] = '#';
            }
            
            rng.shuffleInPlace(dirs);

            for (int i = 0; i < 3; i++) {
                Direction dir = dirs[i];
                switch (dir) {
                    case LEFT:
                        map[rng.between(room.left, x2)][y2] = '.';
                        break;
                    case RIGHT:
                        map[rng.between(x2 + 1, room.right + 1)][y2] = '.';
                        break;
                    case UP:
                        map[x2][rng.between(room.top, y2)] = '.';
                        break;
                    case DOWN:
                        map[x2][rng.between(y2 + 1, room.bottom + 1)] = '.';
                        break;
                    default:
                        throw new IllegalStateException("There should only be cardinal directions here");
                }
            }

            stack.offer(new DividedMazeRoom(room.left, room.top, x2 - 1, y2 - 1));
            stack.offer(new DividedMazeRoom(x2 + 1, room.top, room.right, y2 - 1));
            stack.offer(new DividedMazeRoom(room.left, y2 + 1, x2 - 1, room.bottom));
            stack.offer(new DividedMazeRoom(x2 + 1, y2 + 1, room.right, room.bottom));
        }
    }

}
