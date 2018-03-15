package squidpony;

import regexodus.Matcher;
import regexodus.Pattern;
import regexodus.REFlags;
import squidpony.annotation.Beta;
import squidpony.squidmath.*;

import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static squidpony.ArrayTools.letters;

/**
 * A simple format parser for String-based configuration or data files where JSON is overkill.
 * Supports only one type, String, but allows each String to have arbitrary nested levels of
 * String children as if in sub-lists. You can interpret the Strings however you want, and
 * quoting each String isn't necessary if they are just one word ("bare words" are allowed).
 * This stores its items in an inner class, {@link ObTextEntry}, which only has a "primary"
 * String and may have a List of "associated" ObTextEntry values, each of which must have
 * their own primary String and which may have their own associated List.
 * <br>
 * You can use this like any other List, though it will be contain ObTextEntry objects instead
 * of Strings directly. This allows you to control whether you want to iterate through a
 * particular primary String's associated entries, if there are any, or to skip over them and
 * go to the next String in the current List.
 * <br>
 * This implements List of String and is modifiable; it extends the List interface with
 * {@link #parse(CharSequence)} to read a String in the following format, as well as with some
 * overloads of add() to add just a String with no associated values. A quirk of how this
 * implements List is that it only considers the top-level Strings to be part of the List for
 * length and for {@link #contains(Object)}, and will ignore child strings unless you access
 * them via the {@link ObTextEntry#associated} List on an entry that has associated entries.
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
 * Inspired strongly by STOB, https://github.com/igagis/stob-java and https://github.com/igagis/stob , but
 * no code is shared and the format is slightly different. The main differences are:
 * <ul>
 * <li>We use square brackets in place of STOB's curly braces to mark children associated with a string.<li>
 * <li>ObText supports nested block comments using the syntax {@code /[delimiter/contents/delimiter]/} where
 * delimiter may be empty but must match on both sides, and contents is the body of the comment.</li>
 * <li>ObText uses Python-like "heredoc" syntax for raw strings surrounded by triple-apostrophes '''like so'''
 * with optional initial and final newlines in the raw string ignored. An alternate raw string
 * syntax is present that allows delimiters, using the syntax {@code [[delimiter[contents]delimiter]]}, where 
 * again delimiter may be empty and contents is the body of the raw string.</li>
 * <ul>
 */
@Beta
public class ObText extends ArrayList<ObText.ObTextEntry> implements Serializable{
    private static final long serialVersionUID = 6L;
    public static class ObTextEntry implements Serializable
    {
        private static final long serialVersionUID = 5L;
        public String primary;
        public ArrayList<ObTextEntry> associated;
        public ObTextEntry()
        {
        }
        public ObTextEntry(String primaryString)
        {
            primary = primaryString;
        }
        public ObTextEntry(String primaryString, Collection<ObTextEntry> associatedStrings)
        {
            primary = primaryString;
            associated = new ArrayList<>(associatedStrings);
        }
        public void add(ObTextEntry entry)
        {
            if(associated == null)
                associated = new ArrayList<>(16);
            associated.add(entry);
        }
        public void add(String text)
        {
            if(associated == null)
                associated = new ArrayList<>(16);
            associated.add(new ObTextEntry(text));
        }
        public boolean hasAssociated()
        {
            return associated != null && !associated.isEmpty();
        }
        public List<ObTextEntry> openAssociated()
        {
            if(associated == null)
                associated = new ArrayList<>(16);
            return associated;
        }
        public String firstAssociatedString()
        {
            ObTextEntry got;
            if(associated == null || associated.isEmpty() || (got = associated.get(0)) == null)
                return null;
            return got.primary;
        }
        public ArrayList<String> allAssociatedStrings()
        {
            ObTextEntry got;
            if(associated == null || associated.isEmpty() || (got = associated.get(0)) == null)
                return new ArrayList<>(1);
            int sz = got.associated.size();
            ArrayList<String> strings = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                strings.add(got.associated.get(i).primary);
            }
            return strings;
        }
        public ObTextEntry firstAssociatedEntry()
        {
            if(associated == null || associated.isEmpty())
                return null;
            return associated.get(0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ObTextEntry entry = (ObTextEntry) o;

            if (!primary.equals(entry.primary)) return false;
            return associated != null ? associated.equals(entry.associated) : entry.associated == null;
        }

        public long hash64() {
            long result = CrossHash.hash64(primary), a = 0x632BE59BD9B4E019L;
            if(associated == null)
                return result ^ a;
            final int len = associated.size();
            for (int i = 0; i < len; i++) {
                result += (a ^= 0x8329C6EB9E6AD3E3L * associated.get(i).hash64());
            }
            return result * (a | 1L) ^ (result >>> 27 | result << 37);
        }

        @Override
        public int hashCode() {
            return (int)hash64();
        }
    }
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
    ),
            patternRelaxed = Pattern.compile(
                    "(?>'''(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?({=s}.*?)(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?''')" +
                            "|(?>\\[\\[({=q}[^\\[\\]]*)\\[(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?({=s}.*?)(?:[\n\u000C\f\r\u0085\u2028\u2029]|\r\n)?\\]{\\q}\\]\\])" +
                            "|(?>({=q}[\"'])({=s}.*?)(?<!\\\\){\\q})" +
                            //"|(?>(?>//|#)(?>\\V*))" +
                            //"|(?>/\\*(?:.*?)\\*/)" +
                            //"|(?>/\\[({=q}\\S*)/(?:.*?)/{\\q}\\]/)" +
                            "|({=s}[^\\s\\[\\]\"'\\\\]+)"
                    , REFlags.DOTALL | REFlags.UNICODE
            );


    public static final int stringId = pattern.groupId("s"),
            openId = pattern.groupId("o"), closeId = pattern.groupId("c");

    protected static final Pattern illegalBareWord = Pattern.compile("[\\s\\[\\]\"'#\\\\]|(?:/[/\\*])"),
            reallyIllegalBareWord = Pattern.compile("[\\s\\[\\]\"'\\\\]"),
            needsRaw = Pattern.compile("(?<!\\\\)[\\[\\]]|\\\\$");
    protected static final Matcher m = pattern.matcher();
    protected static final Matcher bare = illegalBareWord.matcher(), raw = needsRaw.matcher(),
            reallyBare = reallyIllegalBareWord.matcher();

    //protected final ArrayList<ObTextEntry> entries = new ArrayList<ObTextEntry>(64);
//    protected final IntVLA neighbors = new IntVLA(64);
//    private final IntVLA nesting = new IntVLA(16);
//    protected int length = 0;
    public ObText()
    {

    }
    
    public ObText(CharSequence text)
    {
        parse(text);
    }

    /**
     * Parses the given text (a String or other CharSequence) and appends it into this ObText.
     * @param text a CharSequence (such as a String) using ObText formatting, as described in this class' JavaDocs
     * @return this ObText object after appending the parsed text, for chaining
     */
    public ObText parse(CharSequence text)
    {
        m.setTarget(text);
        ObTextEntry current = null;
        List<ObTextEntry> ls = this;
        IntVLA nesting = new IntVLA(4);
        nesting.add(-1);
        int t = -1, depth = 0;
        while (m.find()) {
            if (m.isCaptured(stringId)) {
                ls.add(current = new ObTextEntry(m.group(stringId)));
                nesting.incr(depth, 1);
            }
            else if(m.isCaptured(openId))
            {
                if(current == null) throw new UnsupportedOperationException("ObText entries can't have associated items without a primary String.");
                nesting.add(-1);
                ls = current.openAssociated();
                depth++;
            }
            else if(m.isCaptured(closeId))
            {
                if(nesting.size <= 1) throw new UnsupportedOperationException("Associated item sequences in ObText can't end more times than they start.");
                nesting.pop();
                depth--;
                ls = this;
                for (int i = 1; i < nesting.size; i++) {
                    ls = ls.get(nesting.get(i)).associated;
                }
                nesting.incr(depth, 1);
            }
        }
        return this;
    }

    /**
     * Inserts the given String element at the specified position in this ObText's top level.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     * @param index index at which the specified element is to be inserted 
     * @param text String element to be inserted, without any associated entries
     */
    public void add(int index, String text) {
        super.add(index, new ObTextEntry(text));
    }

    /**
     * Appends the given String element to the end of this ObText at the top level.
     * @param text String element to be inserted, without any associated entries
     * @return {@code true} (this always modifies the ObText)
     */
    public boolean add(String text) {
        return super.add(new ObTextEntry(text));
    }
    
    public long hash64()
    {
        long result = 0x9E3779B97F4A7C94L, a = 0x632BE59BD9B4E019L;
        final int len = size();
        for (int i = 0; i < len; i++) {
            result += (a ^= 0x8329C6EB9E6AD3E3L * get(i).hash64());
        }
        return result * (a | 1L) ^ (result >>> 27 | result << 37);

    }
    
    @Override
    public int hashCode() {
        return (int)hash64();
    }

    // Used to generate randomized delimiters using up to 9 non-English letters.
    // call while assigning your state with randomChars(state += 0x9E3779B97F4A7C15L, myChars)
    // that assumes you have a 9-element char[] called myChars
    // as long as z/state is deterministic (i.e. based on a hash), this should be too
    private static void randomChars(long z, char[] mut)
    {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= (z >>> 31);

        mut[0] = letters[(int)(128 + (z & 127))];
        mut[1] = letters[(int)(128 + (z >>> 7 & 127))];
        mut[2] = letters[(int)(128 + (z >>> 14 & 127))];
        mut[3] = letters[(int)(128 + (z >>> 21 & 127))];
        mut[4] = letters[(int)(128 + (z >>> 28 & 127))];
        mut[5] = letters[(int)(128 + (z >>> 35 & 127))];
        mut[6] = letters[(int)(128 + (z >>> 42 & 127))];
        mut[7] = letters[(int)(128 + (z >>> 49 & 127))];
        mut[8] = letters[(int)(128 + (z >>> 56 & 127))];
    }

    public static void appendQuoted(StringBuilder sb, String text)
    {
        appendQuoted(sb, text, reallyBare);
    }

    public static void appendQuotedObText(StringBuilder sb, String text)
    {
        appendQuoted(sb, text, bare);
    }
    protected static void appendQuoted(StringBuilder sb, String text, Matcher bareFinder)
    {
        if(text == null || text.isEmpty()) {
            sb.append("''");
            return;
        }
        bareFinder.setTarget(text);
        if(!bareFinder.find())
            sb.append(text);
        else
        {
            raw.setTarget(text);
            if(raw.find()) {

                if (text.contains("'''")) {
                    long state = CrossHash.hash64(text);
                    char[] myChars = new char[9];
                    int count;
                    do {
                        randomChars(state += 0x9E3779B97F4A7C15L, myChars);
                        count = StringKit.containsPart(text, myChars, "]", "]]");
                    } while (count == 12);
                    sb.append("[[").append(myChars, 0, count).append("[\n").append(text).append("\n]")
                            .append(myChars, 0, count).append("]]");
                } else {
                    sb.append("'''\n").append(text).append("\n'''");
                }
            }
            else if(!text.contains("'"))
            {
                sb.append('\'').append(text).append('\'');
            }
            else
            {
                if(text.contains("\""))
                {
                    if(text.contains("'''"))
                    {
                        long state = CrossHash.hash64(text);
                        char[] myChars = new char[9];
                        int count;
                        do
                        {
                            randomChars(state += 0x9E3779B97F4A7C15L, myChars);
                            count = StringKit.containsPart(text, myChars);
                        }while(count == 9);
                        sb.append("[[").append(myChars, 0, count).append("[\n").append(text).append("\n]")
                                .append(myChars, 0, count).append("]]");
                    }
                    else
                    {
                        sb.append("'''\n").append(text).append("\n'''");
                    }
                }
                else
                {
                    sb.append('"').append(text).append('"');
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ObText object: [[[[\n" + serializeToString() + "\n]]]]";
    }
    
    public String serializeToString()
    {
        StringBuilder sb = new StringBuilder(100);
        iterate(sb, this);
        return sb.toString();
    }

    /**
     * Deserializes an ObText that was serialized by {@link #serializeToString()} or {@link #toString()}, and will
     * ignore the prefix and suffix that toString appends for readability (these are "ObText object: [[[[ " and " ]]]]",
     * for reference). This is otherwise the same as calling the constructor {@link #ObText(CharSequence)}.
     * @param data a String that is usually produced by serializeToString or toString on an ObText
     * @return a new ObText produced by parsing data (disregarding any prefix or suffix from toString() )
     */
    public static ObText deserializeFromString(String data)
    {
        if(data.startsWith("ObText object: [[[[\n"))
        {
            return new ObText(data.substring(20, data.length() - 5));
        }
        return new ObText(data);
    }

    private static void iterate(StringBuilder sb, List<ObTextEntry> it)
    {
        int len = it.size();
        ObTextEntry entry;
        for (int i = 0; i < len; i++) {
            appendQuotedObText(sb, (entry = it.get(i)).primary);
            sb.append('\n');
            if(entry.hasAssociated())
            {
                sb.append("[\n");
                iterate(sb, entry.associated);
                sb.append("]\n");
            }
        }
    }

    /**
     * Gets all Strings from the top level of this ObText, not including any associated values, and puts them in
     * an {@link ArrayList} of String. The returned lisy will retain the same order the Strings were entered in, and
     * unlike {@link #keySet()} or {@link #keyOrderedSet()}, duplicate keys will all be preserved. Changes to the
     * returned List won't be reflected in this ObText.
     * @return all top-level Strings (without associated values) as an ArrayList of String
     */
    public ArrayList<String> keyList()
    {
        final int sz = size();
        ArrayList<String> keys = new ArrayList<>(sz);
        for (int i = 0; i < sz; i++) {
            keys.add(get(i).primary);
        }
        return keys;
    }

    /**
     * Gets all unique Strings from the top level of this ObText, not including any associated values, and puts them in
     * an {@link OrderedSet} of String. The returned set will retain the same order the Strings were entered in, and you
     * can use OrderedSet methods like {@link OrderedSet#getAt(int)} to look up keys by index. Changes to the returned
     * Set won't be reflected in this ObText.
     * @return all unique top-level Strings (without associated values) as an OrderedSet of String keys
     */
    public OrderedSet<String> keyOrderedSet()
    {
        final int sz = size();
        OrderedSet<String> keys = new OrderedSet<>(sz, CrossHash.stringHasher);
        for (int i = 0; i < sz; i++) {
            keys.add(get(i).primary);
        }
        return keys;
    }

    /**
     * Gets all unique Strings from the top level of this ObText, not including any associated values, and puts them in
     * an {@link UnorderedSet} of String. Although the returned set won't be insertion-ordered or necessarily retain the
     * same order the Strings were entered in, the order will be the same on different JVMs, including GWT, because the
     * hashing algorithm is set to one that behaves the same across JVM versions. Changes to the returned Set won't be
     * reflected in this ObText.
     * @return all unique top-level Strings (without associated values) as an UnorderedSet of String keys
     */
    public UnorderedSet<String> keySet()
    {
        final int sz = size();
        UnorderedSet<String> keys = new UnorderedSet<>(sz, CrossHash.stringHasher);
        for (int i = 0; i < sz; i++) {
            keys.add(get(i).primary);
        }
        return keys;
    }
    /**
     * Gets all unique Strings from the top level of this ObText as keys in an {@link OrderedMap}, with the first String
     * associated with each key as its value (or null if nothing is associated with a key String). The returned map will
     * retain the same order the keys were entered in, and you can use OrderedMap methods like
     * {@link OrderedMap#keyAt(int)} to look up keys by index or {@link OrderedMap#getAt(int)} to look up value String
     * by index. Changes to the returned Map won't be reflected in this ObText.
     * @return an OrderedMap of unique String keys associated with the first associated String for each key (or null)
     */
    public OrderedMap<String, String> basicOrderedMap()
    {
        final int sz = size();
        OrderedMap<String, String> keys = new OrderedMap<>(sz, CrossHash.stringHasher);
        ObTextEntry got;
        for (int i = 0; i < sz; i++) {
            got = get(i);
            keys.put(got.primary, got.firstAssociatedString());
        }
        return keys;
    }
    /**
     * Gets all unique Strings from the top level of this ObText as keys in an {@link UnorderedMap}, with the first
     * String associated with each key as its value (or null if nothing is associated with a key String). Although the
     * returned map won't be insertion-ordered or necessarily retain the same order the Strings were entered in, the
     * order will be the same on different JVMs, including GWT, because the hashing algorithm is set to one that behaves
     * the same across JVM versions. Changes to the returned Map won't be reflected in this ObText.
     * @return an UnorderedMap of unique String keys associated with the first associated String for each key (or null)
     */
    public UnorderedMap<String, String> basicMap()
    {
        final int sz = size();
        UnorderedMap<String, String> keys = new UnorderedMap<>(sz, CrossHash.stringHasher);
        ObTextEntry got;
        for (int i = 0; i < sz; i++) {
            got = get(i);
            keys.put(got.primary, got.firstAssociatedString());
        }
        return keys;
    }

    /**
     * Gets all unique Strings from the top level of this ObText as keys in an {@link OrderedMap}, with any Strings
     * associated with those keys as their values (in a possibly-empty ArrayList of String for each value).
     * The returned map will retain the same order the keys were entered in, and you can use OrderedMap methods like
     * {@link OrderedMap#keyAt(int)} to look up keys by index or {@link OrderedMap#getAt(int)} to look up the ArrayList
     * of value Strings by index. Changes to the returned Map won't be reflected in this ObText.
     * @return an OrderedMap of unique String keys associated with ArrayList values containing associated Strings
     */
    public OrderedMap<String, ArrayList<String>> shallowOrderedMap()
    {
        final int sz = size();
        OrderedMap<String, ArrayList<String>> keys = new OrderedMap<>(sz, CrossHash.stringHasher);
        ObTextEntry got;
        for (int i = 0; i < sz; i++) {
            got = get(i);
            keys.put(got.primary, got.allAssociatedStrings());
        }
        return keys;
    }
    /**
     * Gets all unique Strings from the top level of this ObText as keys in an {@link UnorderedMap}, with any Strings
     * associated with those keys as their values (in a possibly-empty ArrayList of String for each value).
     * Although the returned map won't be insertion-ordered or necessarily retain the same order the Strings were
     * entered in, the order will be the same on different JVMs, including GWT, because the hashing algorithm is set to
     * one that behaves the same across JVM versions. Changes to the returned Map won't be reflected in this ObText.
     * @return an UnorderedMap of unique String keys associated with ArrayList values containing associated Strings
     */
    public UnorderedMap<String, ArrayList<String>> shallowMap()
    {
        final int sz = size();
        UnorderedMap<String, ArrayList<String>> keys = new UnorderedMap<>(sz, CrossHash.stringHasher);
        ObTextEntry got;
        for (int i = 0; i < sz; i++) {
            got = get(i);
            keys.put(got.primary, got.allAssociatedStrings());
        }
        return keys;
    }

    /**
     * Can be used to help reading sequences of Strings with ObText-style quotation marking their boundaries.
     * This returns a {@link ContentMatcher} object that you must call setTarget on before using it.
     * The argument(s) to setTarget should be the text that might contain quotes, heredoc-style quotes, or just bare
     * words. Calling {@link ContentMatcher#find()} will try to find the next String, returning false if there's nothing
     * left or returning true and advancing the search if a String was found. The String might be a special term in some
     * cases, like "[" and "]" without quotes being syntax in ObText that don't contain usable Strings. That's why,
     * after a String was found with find(), you should check {@link ContentMatcher#hasMatch()} to verify that a match
     * was successful, and if that's true, then you can call {@link ContentMatcher#getMatch()} to get the un-quoted
     * contents of the next String in the target.
     * @return a {@link ContentMatcher} that must have one of its setTarget() methods called before it can be used
     */
    public static ContentMatcher makeMatcher()
    {
        return new ContentMatcher();
    }
    /**
     * Can be used to help reading sequences of Strings with ObText-style quotation marking their boundaries.
     * This returns a {@link ContentMatcher} object that is already configured to read from {@code text}.
     * The {@code text} should contain Strings that may be surrounded by quotes, heredoc-style quotes, or just bare
     * words. Calling {@link ContentMatcher#find()} will try to find the next String, returning false if there's nothing
     * left or returning true and advancing the search if a String was found. The String might be a special term in some
     * cases, like "[" and "]" without quotes being syntax in ObText that don't contain usable Strings. That's why,
     * after a String was found with find(), you should check {@link ContentMatcher#hasMatch()} to verify that a match
     * was successful, and if that's true, then you can call {@link ContentMatcher#getMatch()} to get the un-quoted
     * contents of the next String in the target.
     * @param text the target String that should probably have at least one sub-string that might be quoted
     * @return a {@link ContentMatcher} that can be used immediately by calling {@link ContentMatcher#find()}
     */
    public static ContentMatcher makeMatcher(CharSequence text)
    {
        return new ContentMatcher(text);
    }

    /**
     * Can be used to help reading sequences of Strings with ObText-style quotation marking their boundaries, but no
     * comments (which allows some additional characters to be used in bare words, like '#').
     * This returns a {@link ContentMatcher} object that is already configured to read from {@code text}.
     * The {@code text} should contain Strings that may be surrounded by quotes, heredoc-style quotes, or just bare
     * words. Calling {@link ContentMatcher#find()} will try to find the next String, returning false if there's nothing
     * left or returning true and advancing the search if a String was found. Unlike the ContentMatcher produced by
     * {@link #makeMatcher(CharSequence)}, you can call {@link ContentMatcher#getMatch()} after any successful call to
     * {@link ContentMatcher#find()}, which will get the un-quoted contents of the next String in the target.
     * @param text the target String that should probably have at least one sub-string that might be quoted
     * @return a {@link ContentMatcher} that can be used immediately by calling {@link ContentMatcher#find()}
     */
    public static ContentMatcher makeMatcherNoComments(CharSequence text)
    {
        return new ContentMatcher(text, patternRelaxed);
    }

    public static class ContentMatcher extends Matcher {

        /**
         * Constructs a ContentMatcher that will need to have its target set with {@link #setTarget(CharSequence)} or
         * one of its overloads. The target should contain multiple substrings that may have quotation around them; this
         * class is meant to skip the quotation in ObText's style.
         */
        public ContentMatcher()
        {
            super(pattern);
        }

        /**
         * Constructs a ContentMatcher that already has its target set to {@code text}.
         * @param text the CharSequence, such as a String, to find possibly-quoted Strings in.
         */
        public ContentMatcher(CharSequence text)
        {
            super(pattern, text);
        }
        /**
         * Constructs a ContentMatcher that already has its target set to {@code text} and uses an alternate Pattern.
         */
        ContentMatcher(CharSequence text, Pattern altPattern)
        {
            super(altPattern, text);
        }


        /**
         * Supplies a text to search in/match with.
         * Resets current search position to zero.
         *
         * @param text - a data
         * @see Matcher#setTarget(Matcher, int)
         * @see Matcher#setTarget(CharSequence, int, int)
         * @see Matcher#setTarget(char[], int, int)
         * @see Matcher#setTarget(Reader, int)
         */
        @Override
        public void setTarget(CharSequence text) {
            super.setTarget(text);
        }

        /**
         * Supplies a text to search in/match with, as a part of String.
         * Resets current search position to zero.
         *
         * @param text  - a data source
         * @param start - where the target starts
         * @param len   - how long is the target
         * @see Matcher#setTarget(Matcher, int)
         * @see Matcher#setTarget(CharSequence)
         * @see Matcher#setTarget(char[], int, int)
         * @see Matcher#setTarget(Reader, int)
         */
        @Override
        public void setTarget(CharSequence text, int start, int len) {
            super.setTarget(text, start, len);
        }

        /**
         * Supplies a text to search in/match with, as a part of char array.
         * Resets current search position to zero.
         *
         * @param text  - a data source
         * @param start - where the target starts
         * @param len   - how long is the target
         * @see Matcher#setTarget(Matcher, int)
         * @see Matcher#setTarget(CharSequence)
         * @see Matcher#setTarget(CharSequence, int, int)
         * @see Matcher#setTarget(Reader, int)
         */
        @Override
        public void setTarget(char[] text, int start, int len) {
            super.setTarget(text, start, len);
        }

        /**
         * Returns true if {@link #find()} has returned true and the found text is a usable String (not some syntax).
         * If this returns true, you can reasonably get a (possibly empty) String using {@link #getMatch()}.
         * @return true if there is a usable String found that can be obtained with {@link #getMatch()}
         */
        public boolean hasMatch()
        {
            return isCaptured(stringId);
        }

        /**
         * Returns the contents of the latest String successfully found with {@link #find()}, without quotation.
         * You should typically call {@link #hasMatch()} even if find() has returned true, to ensure there is a valid
         * String that can be acquired (this will return an empty String if hasMatch() returns false, but an empty
         * String is also potentially a valid result in a successful match, so it should be distinguished).
         * @return the contents of the latest String successfully found with {@link #find()}
         */
        public String getMatch()
        {
            return group(stringId);
        }
    }

}
