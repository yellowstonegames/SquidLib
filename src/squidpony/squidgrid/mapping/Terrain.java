package squidpony.squidgrid.mapping;

import squidpony.SColor;
import squidpony.annotation.Beta;

/**
 * A base class for cell level features common to roguelike dungeons.
 *
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
@Beta
public class Terrain {

    public static final Terrain FLOOR = new Terrain('.', SColor.SLATE_GRAY),
            WALL = new Terrain('#', SColor.CREAM),
            CLOSED_DOOR = new Terrain('+', SColor.BROWNER),
            OPEN_DOOR = new Terrain('/', SColor.BROWNER),
            EMPTY_SPACE = new Terrain('_', SColor.AMETHYST),
            LIQUID = new Terrain('~', SColor.AZUL),
            ENTRANCE = new Terrain('<', SColor.FADED_SEN_NO_RIKYUS_TEA),
            EXIT = new Terrain('>', SColor.GRAPE_MOUSE);
    private final char symbol;
    private final SColor color;

    private Terrain(char symbol, SColor color) {
        this.symbol = symbol;
        this.color = color;
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
    public SColor color() {
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
