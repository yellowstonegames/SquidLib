package squidpony.examples.snowman;

/**
 * This class represents some item of treasure found in the game.
 *
 * @author Eben Howard
 */
public class Treasure {

    private final String name;
    private final int value;

    /**
     * Creates a treasure with the provided name and value.
     *
     * @param name
     * @param value
     */
    public Treasure(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

}
