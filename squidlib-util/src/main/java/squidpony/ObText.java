package squidpony;

import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.squidmath.IntVLA;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A simple format parser for String-based configuration or data files where JSON is overkill.
 * Supports only one type, String, but allows each String to have arbitrary nested levels of
 * String children as if in sub-lists. You can interpret the Strings however you want, and
 * quoting each String isn't necessary if they are just one word ("bare words" are allowed).
 * <br>
 * Inspired strongly by STOB, http://stobml.org/ , but no code is shared and the format is
 * slightly different. The main differences are that ObText supports nested block comments
 * using the syntax /[delimiter/comment here/delimiter]/, and that it uses Python-like
 * "heredoc" syntax for raw strings surrounded by triple-apostrophes '''like this''' with
 * optional initial and final newlines in the raw string ignored. We use square brackets in
 * place of curly braces to mark children associated with a string.
 */
public class ObText implements Iterable<String>{
    public static final Pattern pattern = Pattern.compile(
            "(?>'''(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?({=s}[\\d\\D]*?)(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?''')" +
            "|(?>'''({=s}[\\d\\D]*?)''')" +
            "|(?>({=q}[\"'])({=s}[\\d\\D]*?)(?<!\\\\){\\q})" +
            "|({=comment}(?>//|^#!)(?>\\V*))" +
            "|({=block}/\\[({=q}\\S*)/(?:[\\d\\D]*?)/{\\q}\\]/)" +
            "|({=s}[^\\s\\[\\]\"'#\\\\]+)" +
            "|({=o}\\[)" +
            "|({=c}\\])"

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
     * A one-way iterator through this ObText's String items; note that this can be
     * instructed to descend into child sequences with {@link #child()}, which should
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
            index = i % neighbors.size;
            current = index - 1;
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
         * (In other words, returns {@code true} if {@link #child} would
         * return an element rather than throwing an exception.)
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
            int i;
            if(index >= neighbors.size || (i = neighbors.get(current)) <= 0)
                throw new java.util.NoSuchElementException("No more sibling items in ObText object");
            index = i + (current = index);
            return strings.get(current);
        }
        /**
         * Returns the first child of this ObText.Item and descends into the sequence of child elements.
         *
         * @return the first child of this item
         * @throws java.util.NoSuchElementException if the iteration has no children
         */
        public String child() {
            if(current < 0)
            {
                if(neighbors.size <= 0 || neighbors.get(0) == 1)
                    throw new java.util.NoSuchElementException("No more sibling items in ObText object");
                current = 0;
                index = 1;
                return strings.get(1);
            }
            if(current >= neighbors.size - 2 || neighbors.get(current) == 1)
                throw new java.util.NoSuchElementException("No more sibling items in ObText object");
            index = ++current + 1;
            return strings.get(current);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported");

        }
    }

}
