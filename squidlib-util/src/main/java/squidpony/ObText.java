package squidpony;

import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import squidpony.annotation.Beta;
import squidpony.squidmath.IntVLA;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple format parser for String-based configuration or data files where JSON is overkill.
 * Supports only one type, String, but allows each String to have arbitrary nested levels of
 * String children as if in sub-lists. You can interpret the Strings however you want, and
 * quoting each String isn't necessary if they are just one word ("bare words" are allowed).
 * The main way of using this is to get an ObText.ItemIterator value using {@link #iterator()},
 * which acts as a normal Iterator over the top-level Strings (not children of anything), but
 * to call its {@link ItemIterator#hasChild()} method when you expect potential child elements,
 * then {@link ItemIterator#children()} to get another ItemIterator over the child elements if
 * you want to explore deeper.
 * <br>
 * Format example:
 * <pre>
 * hello world
 * 'how are you today?' [just great thanks]
 * hooray!
 *
 * complexity?
 * [it is possible [yes this is a good example]
 * 'escapes like \[\'\] all work'
 * ]
 *
 * comments are allowed // like this
 * comments can have different forms # like this
 * // block comments like in c are allowed
 * / * but because this example is in javadoc, this example is not actually a comment * /
 * // remove the spaces between each slash and asterisk to make the last line a comment.
 * /[delimit/or block comments with delimiters/delimit]/
 *
 * '''
 * raw strings (heredocs) look like this normally.
 *     they permit characters without escapes, ]][][[ \/\/\ ,
 *     except for triple quotes.
 *     they keep newlines and indentation intact,
 * except for up to one newline ignored adjacent to each triple quote.
 * '''
 *
 * [[different[
 * if you may need triple quotes
 *     in the raw string, use a different syntax that allows delimiters.
 * here, the delimiter is '''different''', just to be different.]different]]
 * </pre>
 * <br>
 * Inspired strongly by STOB, http://stobml.org/ , but no code is shared and the format is
 * slightly different. The main differences are that ObText supports nested block comments
 * using the syntax {@code /[delimiter/contents/delimiter]/} where delimiter may be empty
 * but must match on both sides, and contents is the body of the comment. ObText uses Python-
 * like "heredoc" syntax for raw strings surrounded by triple-apostrophes '''like so''' with
 * optional initial and final newlines in the raw string ignored. An alternate raw string
 * syntax is present that allows delimiters, using the syntax
 * {@code [[delimiter[contents]delimiter]]}, where again delimiter may be empty and contents
 * is the body of the raw string. We use square brackets in place of STOB's curly braces to
 * mark children associated with a string.
 */
@Beta
public class ObText implements Iterable<String>{
    public static final Pattern pattern = Pattern.compile(
            "(?>'''(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?({=s}.*?)(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?''')" +
            "|(?>\\[\\[({=q}[^\\[\\]]*)\\[(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?({=s}.*?)(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?\\]{\\q}\\]\\])" +
            "|(?>({=q}[\"'])({=s}.*?)(?<!\\\\){\\q})" +
            "|(?>(?>//|#)(?>\\V*))" +
            "|(?>/\\*(?:.*?)\\*/)" +
            "|(?>/\\[({=q}\\S*)/(?:.*?)/{\\q}\\]/)" +
            "|({=s}[^\\s\\[\\]\"'#\\\\]+)" +
            "|({=o}\\[)" +
            "|({=c}\\])", REFlags.DOTALL | REFlags.UNICODE

    );
    public static final Matcher m = pattern.matcher();

    protected final ArrayList<String> strings = new ArrayList<String>(64);
    protected final IntVLA neighbors = new IntVLA(64);
    private final IntVLA nesting = new IntVLA(16);
    public ObText()
    {

    }
    public ObText(CharSequence text)
    {
        parse(text);
    }
    public ObText parse(CharSequence text)
    {
        m.setTarget(text);
        nesting.clear();
        int t = -1;
        while (m.find()) {
            if (m.isCaptured("s")) {
                strings.add(m.group("s"));
                neighbors.add(1);
            }
            else if(m.isCaptured("o"))
            {
                nesting.add(neighbors.size - 1);
            }
            else if(m.isCaptured("c"))
            {
                neighbors.incr(t = nesting.pop(), neighbors.size - t - 1);
                if(t < neighbors.size - 1)
                    neighbors.set(neighbors.size-1, 0);
            }
        }
        return this;
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override
    public ItemIterator iterator() {
        return new ItemIterator();
    }

    /**
     * A one-way iterator through this ObText's String items. Note that this can get
     * an iterator into a child sequence with {@link #children()}, which should
     * only be called after checking that a child exists with {@link #hasChild()}.
     * {@link #remove()} is not supported, but {@link #next()} and {@link #hasNext()}
     * are, of course.
     */
    public class ItemIterator implements Iterator<String>
    {
        ItemIterator()
        {
        }
        ItemIterator(int i)
        {
            current = i % neighbors.size;
            index = current;
        }
        int index = 0, current = -1;
        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return index < neighbors.size && (current < 0 || neighbors.get(current) > 0);
        }
        /**
         * Returns {@code true} if the ObText.Item has any child elements.
         * (In other words, returns {@code true} if {@link #children} would
         * return an ItemIterator rather than throwing an exception.)
         *
         * @return {@code true} if this has any children
         */
        public boolean hasChild() {
            return index < neighbors.size - 1 && ((current < 0 && neighbors.get(0) > 1) || neighbors.get(current) > 1);
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws java.util.NoSuchElementException if the iteration has no more elements
         */
        @Override
        public String next() {
            if(current < 0)
            {
                if(strings.isEmpty())
                    throw new java.util.NoSuchElementException("No more sibling items in ObText object");
                current = 0;
                index = neighbors.get(0);
                return strings.get(0);
            }
            if(index >= neighbors.size || neighbors.get(current) <= 0)
                throw new java.util.NoSuchElementException("No more sibling items in ObText object");
            index = neighbors.get(index) + (current = index);
            return strings.get(current);
        }
        /**
         * Returns the first child of this ObText.Item and descends into the sequence of child elements.
         *
         * @return the first child of this item
         * @throws java.util.NoSuchElementException if the iteration has no children
         */
        public ItemIterator children() {
            if(current < 0)
            {
                if(neighbors.size <= 0 || neighbors.get(0) == 1)
                    throw new java.util.NoSuchElementException("No current children in ObText object");
                return new ItemIterator(1);
            }
            if(current >= neighbors.size - 2 || neighbors.get(current) == 1)
                throw new java.util.NoSuchElementException("No current children in ObText object");
            return new ItemIterator(current+1);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");

        }
    }

}
