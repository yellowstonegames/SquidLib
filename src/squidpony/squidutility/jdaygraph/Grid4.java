package squidpony.squidutility.jdaygraph;

import java.util.Arrays;
import java.util.HashMap;
import squidpony.squidgrid.util.DirectionIntercardinal; 

/**
 * A topology in which cells are only consider connected by the four main directions. All cells are
 * unique in this mapping, with the exception that some cells may be null.
 *
 * Any invalid operation that returns an index value will return a -1 to indicate invalidity.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Grid4<T> implements Overlay<T, DirectionIntercardinal> {

    private int width, height;
    private Object[] map;
    private final HashMap<T, Integer> cells = new HashMap<>();
    private boolean wrapTop;
    private boolean wrapSide;

    Grid4(int width, int height) {
        this(width, height, false, false);
    }

    public Grid4(int width, int height, boolean wrapTop, boolean wrapSide) {
        this.width = width;
        this.height = height;
        map = new Object[width * height];
        this.wrapTop = wrapTop;
        this.wrapSide = wrapSide;
    }

    /**
     * Converts a 2d coordinate into a 1d index
     *
     * @param x
     * @param y
     * @return
     */
    private int convert2d(int x, int y) {
        return x + y * width;
    }

    /**
     * Gets the x component of the encoded index
     *
     * @param i
     * @return
     */
    private int convertX(int i) {
        return i % width;
    }

    /**
     * Gets the y component of the encoded index
     *
     * @param i
     * @return
     */
    private int convertY(int i) {
        return i / width;
    }

    /**
     * Adds the given cell to the desired location, overwriting any cell that may have previously
     * been there.
     *
     * @param x
     * @param y
     * @param cell
     */
    public void put(int x, int y, T cell) {
        int index = convert2d(x, y);
        if (map[index] != null) {
            cells.remove((T) map[index]);
        }

        map[index] = cell;
        cells.put(cell, index);
    }

    @Override
    public int size() {
        return width * height;
    }

    /**
     * Returns -1 if there is no valid index for the cell.
     *
     * @param t
     * @return
     */
    @Override
    public int indexOf(T t) {
        Integer p = cells.get(t);
        return p == null ? -1 : p;
    }

    @Override
    public T at(int index) {
        return (index >= 0 && index < size()) ? (T) map[index] : null;
    }

    @Override
    public int[] neighbors(int index) {
        int[] ret = new int[DirectionIntercardinal.CARDINALS.length];//most number of neighbors possible
        int i = 0;
        for (DirectionIntercardinal dir : DirectionIntercardinal.CARDINALS) {
            int n = neighbor(index, dir);
            if (n >= 0) {
                ret[i] = n;
                i++;
            }
        }
        return Arrays.copyOf(ret, i);//trimmed to just filled length
    }

    @Override
    public int neighbor(int i, DirectionIntercardinal dir) {
        switch (dir) {
            case UP:
            case DOWN:
            case LEFT:
            case RIGHT:
                int x = convertX(i) + dir.deltaX;
                int y = convertY(i) + dir.deltaY;
                if (wrapSide) {
                    x %= width;
                }
                if (wrapTop) {
                    y %= height;
                }
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    return convert2d(x, y);

                }
        }
        return -1;//either no wrap or invalid direction passed in
    }

    @Override
    public float traversalCost(int index) {
        return map[index] == null ? Float.POSITIVE_INFINITY : 1f;
    }

    @Override
    public float traversalCost(int index, DirectionIntercardinal traversal) {
        switch (traversal) {
            case NONE:
                return 0f;
            case UP:
            case LEFT:
            case RIGHT:
            case DOWN:
                return 1f;
            default:
                return Float.POSITIVE_INFINITY;
        }
    }

}
