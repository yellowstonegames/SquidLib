package squidpony.panel;

/**
 * An interface that lets non-display code request some special rendering for a {@code T} value, and an implementation
 * can handle this appropriately in display code. Much of the time, IMarkup will not be directly used in games, and it
 * may have much of its value in library code that can use it to declare that some value should be shown in larger text,
 * or in a certain color, without specifying the exact details of what shade of what color or precisely how much larger
 * the text should be. However, there may be good uses for highly-specific custom implementations of IMarkup for types
 * present only in one game, where {@code T} may be {@code Creature} or some other class specific to a game, and the
 * IMarkup can be used to get a special String to describe or display that Creature with any color/size markup or even
 * a String that changes over time. The {@link #getMarkup(Object)} method yields the appropriate text or markup to
 * describe its parameter, and the {@link #closeMarkup()} method yields text that ends any block of markup that is
 * ongoing. For the example of an IMarkup for colors, getMarkup would be given a color parameter and would color any
 * text in that color or a variant on it until the markup is closed with closeMarkup, or possibly until the color is
 * specified again via getMarkup. For the example of IMarkup for Creatures, getMarkup would produce a String that
 * describes the Creature it is given as a parameter (if it uses colors/sizes as well, it should close itself to be
 * self-contained), and closeMarkup would produce the empty String.
 * Created by Tommy Ettinger on 1/23/2016.
 */
public interface IMarkup<T> {
    /**
     * Implementations should use this method to get a String that describes the given value, or begins some section of
     * markup that uses a quality specified by value, such as a color or text size.
     * @param value an object of type T that can be described by this IMarkup implementation
     * @return a String either describing the value or starting a section of markup using the value.
     */
    String getMarkup(T value);

    /**
     * Implementations should use this method to get a String that ends any section of markup currently ongoing.
     * This may be the empty String in many cases.
     * @return the requisite String to end any ongoing sections of markup
     */
    String closeMarkup();

    // escape() is currently commented out because I don't know if it is needed.
    /*
     * If the Strings used as markup may occur in normal text, you can call this on the text before applying markup to
     * escape the not-actually-markup Strings in the text. If your variety of markup doesn't have a concept of escaping,
     * this can return initialText as-is.
     * @param initialText the text to process, before applying markup
     * @return a String made from initialText, with any pre-existing Strings that could be read as markup escaped
     * /
    String escape(String initialText);
     */
    /**
     * Probably not that useful on its own, but may be good as an example.
     * @see squidpony.panel.IMarkup IMarkup has more complete documentation on how it should be used
     */
    class StringMarkup implements IMarkup<String>
    {
        @Override
        public String getMarkup(String value) {
            return "[" + value + "]";
        }

        @Override
        public String closeMarkup() {
            return "[]";
        }

        /*
        @Override
        public String escape(String initialText)
        {
            return initialText.replace("[", "[[");
        }
        */
    }
}
