package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;

/**
 * A base class for cell level features common to roguelike dungeons.
 * 
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Terrain {

    public static final Terrain FLOOR = new Terrain('.', 0),
            WALL = new Terrain('#', 1),
            CLOSED_DOOR = new Terrain('+', 2),
            OPEN_DOOR = new Terrain('/', 2),
            EMPTY_SPACE = new Terrain('_', 30),
            LIQUID = new Terrain('~', 26),
            ENTRANCE = new Terrain('<', 34),
            EXIT = new Terrain('>', 38);
    private final char symbol;
    private final int color;

    private Terrain(char symbol, int colorIndex) {
        this.symbol = symbol;
        color = colorIndex;
    }

    /**
     * Returns the character representation for this terrain. This is meant to
     * be for display purposes and should not be used to check for equality
     * since multiple different terrains may return the same symbol.
     *
     * @return the representation of this terrain as a char
     */
    public char symbol() {
        return symbol;
    }

    /**
     * Returns the color for this terrain as an index into some unspecified palette.
     *
     * @return the color for this terrain as an index into some unspecified palette
     */
    public int colorIndex() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Terrain terrain = (Terrain) o;
        
        return symbol == terrain.symbol && color == terrain.color;
    }

    @Override
    public int hashCode() {
        return  31 * (int) symbol + color;
    }
}
