package squidpony.examples.libgdxExample;

import java.awt.Point;

/**
 * This represents a single explorable map level.
 *
 * Each cell is considered to be 1 meter by 1 meter square.
 *
 * A null tile represents open space with no special properties or resistances
 * to things passing through. They should not be considered a vacuum, but rather
 * normal air.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class Map {

    public int width, height;
    public Item[][] contents;
    public boolean[][] hasBeenSeen;

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        contents = new Item[width][height];
        hasBeenSeen = new boolean[width][height];
    }
    
    public Map(Item[][] contents){
        this.contents = contents;
        this.width = contents.length;
        this.height = contents[0].length;
    }


    public boolean inBounds(Point p){
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }
}
