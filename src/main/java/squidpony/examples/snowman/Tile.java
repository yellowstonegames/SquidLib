package squidpony.examples.snowman;

import squidpony.SColor;

/**
 * This class represents a single map space.
 *
 * All spaces are initialy marked as not seen by the player.
 *
 * @author Eben Howard
 */
public class Tile {

    private static final char WALL_SYMBOL = '▒', FLOOR_SYMBOL = '.', UNKNOWN_SYMBOL = ' ';
    private Monster monster;
    private Treasure treasure;
    private boolean seen = false;
    private boolean wall = false;

    /**
     * Creates a tile with no monster or wall.
     */
    public Tile() {
    }

    /**
     * Creates a tile that is a wall.
     *
     * @param wall
     */
    public Tile(boolean wall) {
        this.wall = wall;
    }

    /**
     * Creates a tile with the provided monster and no wall.
     *
     * @param monster
     */
    public Tile(Monster monster) {
        this.monster = monster;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public void setTreasure(Treasure treasure) {
        this.treasure = treasure;
    }

    public Monster getMonster() {
        return monster;
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isWall() {
        return wall;
    }

    /**
     * Returns the symbol that should be used for this tile.
     *
     * @return
     */
    public char getSymbol() {
        if (!seen) {
            return UNKNOWN_SYMBOL;
        }

        if (monster != null) {
            return monster.getSymbol();
        }

        if (treasure != null) {
            return '$';
        }

        if (wall) {
            return WALL_SYMBOL;
        }

        return FLOOR_SYMBOL;
    }

    /**
     * Returns the color that should be used for this tile.
     *
     * @return
     */
    public SColor getColor() {
        if (!seen) {
            return SColor.BLACK;
        }

        if (monster != null) {
            return monster.getColor();
        }

        if (treasure != null) {
            return SColor.GOLDEN;
        }

        if (wall) {
            return SColor.GREYISH_DARK_GREEN;
        }

        return SColor.DARK_SLATE_GRAY;
    }
}
