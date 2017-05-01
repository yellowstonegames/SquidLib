package squidpony.squidgrid.mapping.locks.constraints;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.mapping.locks.util.GenerationFailureException;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

public class ColorMap {
    
    protected int xsum, ysum, xmin, xmax, ymin, ymax;
    protected OrderedMap<Coord, Integer> map;

    public ColorMap() {
        map = new OrderedMap<>();
        ymin = xmin = Integer.MAX_VALUE;
        ymax = xmax = Integer.MIN_VALUE;
    }
    
    public void set(int x, int y, int color) {
        Coord xy = Coord.get(x,y);
        if (map.get(xy) == null) {
            xsum += x;
            ysum += y;
        }
        map.put(xy, color);
        
        if (x < xmin) xmin = x;
        if (x > xmax) xmax = x;
        if (y < ymin) ymin = y;
        if (y > ymax) ymax = y;
    }
    
    public Integer get(int x, int y) {
        return map.get(Coord.get(x,y));
    }
    
    public Coord getCenter() {
        return Coord.get(xsum/map.size(), ysum/map.size());
    }
    
    public int getWidth() {
        return xmax-xmin+1;
    }
    
    public int getHeight() {
        return ymax-ymin+1;
    }
    
    public int getLeft() {
        return xmin;
    }
    
    public int getTop() {
        return ymin;
    }
    
    public int getRight() {
        return xmax;
    }
    
    public int getBottom() {
        return ymax;
    }
    
    protected boolean isConnected() {
        if (map.size() == 0) return false;
        
        // Do a breadth first search starting at the top left to check if
        // every position is reachable.
        OrderedSet<Coord> world = map.keysAsOrderedSet(),
                    queue = new OrderedSet<>();
        
        queue.add(world.removeFirst());
        
        while (!queue.isEmpty()) {
            Coord pos = queue.removeFirst();
            for (Direction d: Direction.CARDINALS) {
                Coord neighbor = pos.translate(d);
                
                if (world.contains(neighbor)) {
                    world.remove(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        
        return world.size() == 0;
    }
    
    public void checkConnected() {
        if (!isConnected()) {
            // Parts of the map are unreachable!
            throw new GenerationFailureException("ColorMap is not fully connected");
        }
    }
    
}
