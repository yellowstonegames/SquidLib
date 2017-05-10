package squidpony.squidgrid.mapping.locks.util;

import squidpony.squidgrid.mapping.Rectangle;
import squidpony.squidmath.Coord;

import java.util.List;

/**
 * Created by Tommy Ettinger on 1/4/2017.
 */
public class Rect2I extends Rectangle.Impl {
    public Coord topLeft;
    public int width;
    public int height;

    public Rect2I(Coord min, int w, int h)
    {
        super(min, w, h);
        topLeft = min;
        width = w;
        height = h;
    }

    public static Rect2I fromExtremes(int minX, int minY, int maxX, int maxY)
    {
        return new Rect2I(Coord.get(minX, minY), maxX - minX, maxY - minY);
    }

    public Coord getBottomLeft() {
        return topLeft;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int left()
    {
        return topLeft.x;
    }
    public int top()
    {
        return topLeft.y;
    }

    public int right()
    {
        return topLeft.x + width;
    }
    public int bottom()
    {
        return topLeft.y + height;
    }

    @Override
    public boolean isEmpty() {
        return width > 0 && height > 0;
    }

    @Override
    public int size() {
        return width * height;
    }

    @Override
    public boolean contains(int x, int y) {
        return x >= topLeft.x && x < topLeft.x + width && y >= topLeft.y && y < topLeft.y + height;
    }

    @Override
    public boolean contains(Coord coord) {
        return coord.x >= topLeft.x && coord.x < topLeft.x + width
                && coord.y >= topLeft.y && coord.y < topLeft.y + height;
    }

    @Override
    public List<Coord> getAll() {
        return Rectangle.Utils.cellsList(this);
    }
}
