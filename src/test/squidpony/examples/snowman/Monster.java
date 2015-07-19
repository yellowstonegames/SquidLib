package squidpony.examples.snowman;

import squidpony.SColor;

/**
 * This class represents an enemy creature.
 *
 * @author Eben Howard
 */
public class Monster {

    public static final Monster //this is a list of the various monster templates
            SNOWMAN = new Monster("squidpony/examples/snowman", 5, '☃', SColor.ALICE_BLUE),
            PLAYER = new Monster("player", 10, '☺', SColor.BRIGHT_TURQUOISE);

    private final String name;
    private int health;
    private final char symbol;
    private final SColor color;
    public int x,y;

    /**
     * Creates a new monster.
     * 
     * @param name
     * @param health
     * @param symbol
     * @param color 
     */
    public Monster(String name, int health, char symbol, SColor color) {
        this.name = name;
        this.health = health;
        this.symbol = symbol;
        this.color = color;
    }

    /**
     * Creates a new Monster that is a clone of the passed in monster.
     *
     * @param other
     */
    public Monster(Monster other) {
        this(other.name, other.health, other.symbol, other.color);
    }

    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public char getSymbol() {
        return symbol;
    }

    public SColor getColor() {
        return color;
    }

    /**
     * Reduces the monster's health by the amount passed in. Returns true if
     * this results in the health being equal to or below zero.
     *
     * @param damage
     * @return
     */
    public boolean causeDamage(int damage) {
        health -= damage;
        return health <= 0;
    }

    @Override
    public String toString() {
        return name + ": " + symbol + " @ " + health;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Monster ? ((Monster) obj).name.equals(name) : false;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + symbol + color.hashCode();
    }

}
