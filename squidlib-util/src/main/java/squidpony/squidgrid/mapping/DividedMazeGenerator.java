package squidpony.squidgrid.mapping;

import squidpony.Maker;
import squidpony.squidgrid.Direction;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.RNG;

import java.util.*;

/**
 * Recursively divided maze. Creates only walls and passages.
 *
 * This dungeon generator is based on a port of the rot.js version.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class DividedMazeGenerator {

    private class DividedMazeRoom {

        private int left, top, right, bottom;

        public DividedMazeRoom(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    private int width, height;
    private boolean[][] map;
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
        rng = new RNG();
    }

    /**
     * Sets up the generator to make mazes the given width and height. The mazes
     * have a solid wall border.
     *
     * @param width in cells
     * @param height in cells
     * @param rng the random number generator to use
     */
    public DividedMazeGenerator(int width, int height, IRNG rng) {
        this.width = width;
        this.height = height;
        this.rng = rng;
    }

    /**
     * Builds a maze. True values represent walls.
     *
     * @return
     */
    public boolean[][] create() {
        map = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = x == 0 || y == 0 || x + 1 == width || y + 1 == height;
            }
        }

        process();

        return map;
    }

    private void process() {
        ArrayDeque<DividedMazeRoom> stack = new ArrayDeque<>();
        stack.offer(new DividedMazeRoom(1, 1, width - 2, height - 2));
        while (!stack.isEmpty()) {
            DividedMazeRoom room = stack.removeFirst();
            ArrayList<Integer> availX = new ArrayList<>(),
                               availY = new ArrayList<>();

            for (int x = room.left + 1; x < room.right; x++) {
                boolean top = map[x][room.top - 1];
                boolean bottom = map[x][room.bottom + 1];
                if (top && bottom && x % 2 == 0) {
                    availX.add(x);
                }
            }

            for (int y = room.top + 1; y < room.bottom; y++) {
                boolean left = map[room.left - 1][y];
                boolean right = map[room.right + 1][y];
                if (left && right && y % 2 == 0) {
                    availY.add(y);
                }
            }

            if (availX.isEmpty() || availY.isEmpty()) {
                continue;
            }

            int x2 = rng.getRandomElement(availX);
            int y2 = rng.getRandomElement(availY);

            map[x2][y2] = true;

            for (Direction dir : Direction.CARDINALS) {
                switch (dir) {
                    case LEFT:
                        for (int x = room.left; x < x2; x++) {
                            map[x][y2] = true;
                        }
                        break;
                    case RIGHT:
                        for (int x = x2 + 1; x <= room.right; x++) {
                            map[x][y2] = true;
                        }
                        break;
                    case UP:
                        for (int y = room.top; y < y2; y++) {
                            map[x2][y] = true;
                        }
                        break;
                    case DOWN:
                        for (int y = y2 + 1; y <= room.bottom; y++) {
                            map[x2][y] = true;
                        }
                        break;
                    case NONE:
                    	break;
				case DOWN_LEFT:
				case DOWN_RIGHT:
				case UP_LEFT:
				case UP_RIGHT:
					throw new IllegalStateException("There should only be cardinal directions here");
                }
            }

            ArrayList<Direction> dirs = Maker.makeList(Direction.CARDINALS);
            dirs.remove(rng.getRandomElement(dirs));

            for (Direction dir : dirs) {
                switch (dir) {
                    case LEFT:
                        map[rng.between(room.left, x2)][y2] = false;
                        break;
                    case RIGHT:
                        map[rng.between(x2 + 1, room.right + 1)][y2] = false;
                        break;
                    case UP:
                        map[x2][rng.between(room.top, y2)] = false;
                        break;
                    case DOWN:
                        map[x2][rng.between(y2 + 1, room.bottom + 1)] = false;
                        break;
                    case NONE:
                    	break;
				case DOWN_LEFT:
				case DOWN_RIGHT:
				case UP_LEFT:
				case UP_RIGHT:
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
