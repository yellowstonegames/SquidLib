package squidpony.squidgrid.mapping;

import squidpony.annotation.Beta;

/**
 * A base class for cell level features common to roguelike dungeons.
 *
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
        this.color = colorIndex;
    }

    /**
     * Returns the character representation for this terrain. This is meant to
     * be for display purposes and should not be used to check for equality
     * since multiple different terrains may return the same symbol.
     *
     * @return
     */
    public char symbol() {
        return symbol;
    }

    /**
     * Returns the color for this terrain.
     *
     * @return
     */
    public int colorIndex() {
        return color;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.symbol;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Terrain other = (Terrain) obj;
        return this.symbol == other.symbol;
    }
}
