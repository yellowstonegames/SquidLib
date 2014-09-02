package squidpony.squidgrid.mapping;

/**
 * An enumeration of the common types of features in roguelikes.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public enum Terrain {

    FLOOR('.'), WALL('#'), CLOSED_DOOR('+'), OPEN_DOOR('/');
    public char symbol;

    private Terrain(char symbol) {
        this.symbol = symbol;
    }

}
