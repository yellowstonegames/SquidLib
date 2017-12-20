package squidpony;

import regexodus.Matcher;
import regexodus.Pattern;
import squidpony.squidmath.CrossHash;
import squidpony.squidmath.NumberTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Various utility functions for dealing with Strings, CharSequences, and char[]s; mostly converting numbers.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {

    /**
     * Searches text for the exact contents of the char array search; returns true if text contains search.
     * @param text a CharSequence, such as a String or StringBuilder, that might contain search
     * @param search a char array to try to find in text
     * @return true if search was found
     */
    public static boolean contains(CharSequence text, char[] search) {
        return !(text == null || text.length() == 0 || search == null || search.length <= 0)
                && containsPart(text, search, "", "") == search.length;
    }

    public static int containsPart(CharSequence text, char[] search)
    {
        return containsPart(text, search, "", "");
    }
    public static int containsPart(CharSequence text, char[] search, CharSequence prefix, CharSequence suffix)
    {
        if(prefix == null) prefix = "";
        if(suffix == null) suffix = "";
        int bl = prefix.length(), el = suffix.length();
        if(text == null || text.length() == 0 || search == null || (search.length + bl + el <= 0))
            return 0;
        int sl = bl + search.length + el, tl = text.length() - sl, f = 0, sl2 = sl - el;
        char s = (bl <= 0) ? (search.length <= 0 ? suffix.charAt(0) : search[0]) : prefix.charAt(0);
        PRIMARY:
        for (int i = 0; i <= tl; i++) {
            if(text.charAt(i) == s)
            {
                for (int j = i+1, x = 1; x < sl; j++, x++) {
                    if(x < bl)
                    {
                        if (text.charAt(j) != prefix.charAt(x)) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                    else if(x < sl2)
                    {
                        if (text.charAt(j) != search[x-bl]) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                    else
                    {
                        if (text.charAt(j) != suffix.charAt(x - sl2)) {
                            f = Math.max(f, x);
                            continue PRIMARY;
                        }
                    }
                }
                return sl;
            }
        }
        return f;
    }

    public static String join(CharSequence delimiter, CharSequence... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, Collection<? extends CharSequence> elements) {
        if (elements == null || elements.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(64);
        Iterator<? extends CharSequence> it = elements.iterator();
        sb.append(it.next());
        while(it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb.toString();
    }

    public static String joinArrays(CharSequence delimiter, char[]... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    public static String join(CharSequence delimiter, long... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, double... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, int... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, float... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, short... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, char... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, byte... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    public static String join(CharSequence delimiter, boolean... elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }

    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. Unlike other join methods in this class, this does
     * not take a vararg of Object items, since that would cause confusion with the overloads that take one object, such
     * as {@link #join(CharSequence, Iterable)}; it takes a non-vararg Object array instead.
     * @param delimiter the String or other CharSequence to separate items in elements with
     * @param elements the Object items to stringify and join into one String; if the array is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Object[] elements) {
        if (elements == null || elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(elements[0]);
        for (int i = 1; i < elements.length; i++) {
            sb.append(delimiter).append(elements[i]);
        }
        return sb.toString();
    }
    /**
     * Joins the items in {@code elements} by calling their toString method on them (or just using the String "null" for
     * null items), and separating each item with {@code delimiter}. This can take any Iterable of any type for its
     * elements parameter.
     * @param delimiter the String or other CharSequence to separate items in elements with
     * @param elements the Object items to stringify and join into one String; if Iterable is null or empty, this
     *                 returns an empty String, and if items are null, they are shown as "null"
     * @return the String representations of the items in elements, separated by delimiter and put in one String
     */
    public static String join(CharSequence delimiter, Iterable<?> elements) {
        if (elements == null) return "";
        Iterator<?> it = elements.iterator();
        if(!it.hasNext()) return "";
        StringBuilder sb = new StringBuilder(64);
        sb.append(it.next());
        while(it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb.toString();
    }

    /**
     * Joins the boolean array {@code elements} without delimiters into a String, using "1" for true and "0" for false.
     * @param elements an array or vararg of booleans
     * @return a String using 1 for true elements and 0 for false, or "N" if elements is null
     */
    public static String joinAlt(boolean... elements) {
        if (elements == null) return "N";
        if(elements.length == 0) return "";
        StringBuilder sb = new StringBuilder(64);
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i] ? '1' : '0');
        }
        return sb.toString();
    }

    /**
     * Scans repeatedly in {@code source} for the String {@code search}, not scanning the same char twice except as part
     * of a larger String, and returns the number of instances of search that were found, or 0 if source is null or if
     * search is null or empty.
     * @param source a String to look through
     * @param search a String to look for
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search)
    {
        if(source == null || search == null || source.isEmpty() || search.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} for the codepoint {@code search} (which is usually a char literal), not
     * scanning the same section twice, and returns the number of instances of search that were found, or 0 if source is
     * null.
     * @param source a String to look through
     * @param search a codepoint or char to look for
     * @return the number of times search was found in source
     */
    public static int count(final String source, final int search)
    {
        if(source == null || source.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }
    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the String {@code search}, not scanning the same char twice except as part of a larger String, and returns the
     * number of instances of search that were found, or 0 if source or search is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param search a String to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    public static int count(final String source, final String search, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || search == null || source.isEmpty() || search.isEmpty()
                || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1;
        while ((idx = source.indexOf(search, idx+1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }

    /**
     * Scans repeatedly in {@code source} (only using the area from startIndex, inclusive, to endIndex, exclusive) for
     * the codepoint {@code search} (which is usually a char literal), not scanning the same section twice, and returns
     * the number of instances of search that were found, or 0 if source is null or if the searched area is empty.
     * If endIndex is negative, this will search from startIndex until the end of the source.
     * @param source a String to look through
     * @param search a codepoint or char to look for
     * @param startIndex the first index to search through, inclusive
     * @param endIndex the last index to search through, exclusive; if negative this will search the rest of source
     * @return the number of times search was found in source
     */
    public static int count(final String source, final int search, final int startIndex, int endIndex)
    {
        if(endIndex < 0) endIndex = 0x7fffffff;
        if(source == null || source.isEmpty() || startIndex < 0 || startIndex >= endIndex)
            return 0;
        int amount = 0, idx = startIndex-1;
        while ((idx = source.indexOf(search, idx+1)) >= 0 && idx < endIndex)
            ++amount;
        return amount;
    }

    /**
     * Like {@link String#substring(int, int)} but returns "" instead of throwing any sort of Exception.
     * @param source the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex the index after the last character (exclusive); if negative this will be source.length()
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex)
    {
        if(source == null || source.isEmpty()) return "";
        if(beginIndex < 0) beginIndex = 0;
        if(endIndex < 0 || endIndex > source.length()) endIndex = source.length();
        if(beginIndex > endIndex) return "";
        return source.substring(beginIndex, endIndex);
    }
    public static final Pattern whitespacePattern = Pattern.compile("\\s+"),
            nonSpacePattern = Pattern.compile("\\S+");
    private static final Matcher matcher = new Matcher(whitespacePattern);
    public static int indexOf(CharSequence text, Pattern regex, int beginIndex)
    {
        matcher.setPattern(regex);
        matcher.setTarget(text);
        matcher.setPosition(beginIndex);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, String regex, int beginIndex)
    {
        matcher.setPattern(Pattern.compile(regex));
        matcher.setTarget(text);
        matcher.setPosition(beginIndex);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, Pattern regex)
    {
        matcher.setPattern(regex);
        matcher.setTarget(text);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }
    public static int indexOf(CharSequence text, String regex)
    {
        matcher.setPattern(Pattern.compile(regex));
        matcher.setTarget(text);
        if(!matcher.find())
            return -1;
        return matcher.start();
    }

    /**
     * Like {@link String#split(String)} but doesn't use any regex for splitting (delimiter is a literal String).
     * @param source the String to get split-up substrings from
     * @param delimiter the literal String to split on (not a regex); will not be included in the returned String array
     * @return a String array consisting of at least one String (all of Source if nothing was split)
     */
    public static String[] split(String source, String delimiter) {
        int amount = count(source, delimiter);
        if (amount <= 0) return new String[]{source};
        String[] splat = new String[amount+1];
        int dl = delimiter.length(), idx = -dl, idx2;
        for (int i = 0; i < amount; i++) {
            splat[i] = safeSubstring(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
        }
        if((idx2 = source.indexOf(delimiter, idx+dl)) < 0)
        {
            splat[amount] = safeSubstring(source, idx+dl, source.length());
        }
        else
        {
            splat[amount] = safeSubstring(source, idx+dl, idx2);
        }
        return splat;
    }

    public static final String mask64 = "0000000000000000000000000000000000000000000000000000000000000000",
            mask32 = "00000000000000000000000000000000",
            mask16 = "0000000000000000",
            mask8 = "00000000";

    private static final char[] keyBase64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray(),
            valBase64 = new char[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                    62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 64, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
                    0, 0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51};

    public static String hex(long number) {
        String h = Long.toHexString(number);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String hex(int number) {
        String h = Integer.toHexString(number);
        return mask8.substring(0, 8 - h.length()) + h;
    }

    public static String hex(short number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask8.substring(4, 8 - h.length()) + h;
    }

    public static String hex(char number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask8.substring(4, 8 - h.length()) + h;
    }

    public static String hex(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(6, 8 - h.length()) + h;
    }

    public static String hex(long[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 4);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(int[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 3);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(short[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(char[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 2);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String hex(byte[] numbers) {
        int len;
        if (numbers == null || (len = numbers.length) <= 0) return "";
        StringBuilder sb = new StringBuilder(numbers.length << 1);
        for (int i = 0; i < len; i++) {
            sb.append(hex(numbers[i]));
        }
        return sb.toString();
    }

    public static String bin(long number) {
        String h = Long.toBinaryString(number);
        return mask64.substring(0, 64 - h.length()) + h;
    }

    public static String bin(int number) {
        String h = Integer.toBinaryString(number);
        return mask32.substring(0, 32 - h.length()) + h;
    }

    public static String bin(short number) {
        String h = Integer.toBinaryString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(char number) {
        String h = Integer.toBinaryString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(byte number) {
        String h = Integer.toBinaryString(number & 0xff);
        return mask8.substring(0, 8 - h.length()) + h;
    }
    private static final int[] hexCodes = new int[]
            {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
             -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
             -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
              0, 1, 2, 3, 4, 5, 6, 7, 8, 9,-1,-1,-1,-1,-1,-1,
             -1,10,11,12,13,14,15,-1,-1,-1,-1,-1,-1,-1,-1,-1,
             -1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,
             -1,10,11,12,13,14,15};

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given a %x in the formatting
     * string, or this class' {@link #hex(long)} method; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1 ".
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before the end of cs is reached. If the parse is
     * stopped early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger
     * places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs) {
        return longFromHex(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format given a %x in the formatting
     * string, or this class' {@link #hex(long)} method; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1 ". If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
        int len, h, lim = 16;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            len = -1;
            h = 0;
            lim = 17;
        } else if (c == '+') {
            len = 1;
            h = 0;
            lim = 17;
        } else if (c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else {
            len = 1;
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if ((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }
    /**
     * Reads in a char[] containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start and
     * returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(long)} method; that is, if the first digit of a 16-char (or longer) char[] is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFFFFFFFFFF" would return the long -1L when passed to this, though you could also simply use "-1 ".
     * If you use both '-' at the start and have the most significant digit as 8 or higher, such as with
     * "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first (producing -1)
     * and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a char array containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the long that cs represents
     */
    public static long longFromHex(final char[] cs, final int start, int end)
    {
        int len, h, lim = 16;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 17;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 17;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = hexCodes[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) CharSequence is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before the end of cs is reached. If the parse is
     * stopped early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger
     * places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs) {
        return intFromHex(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) CharSequence is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 8;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 9;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 9;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }
    /**
     * Reads in a char[] containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the result
     * if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also represent
     * negative numbers as they are printed by such methods as String.format given a %x in the formatting string, or
     * this class' {@link #hex(int)} method; that is, if the first digit of an 8-char (or longer) char[] is a hex
     * digit 8 or higher, then the whole number represents a negative number, using two's complement and so on. This
     * means "FFFFFFFF" would return the int -1 when passed to this, though you could also simply use "-1 ". If you use
     * both '-' at the start and have the most significant digit as 8 or higher, such as with "-FFFFFFFF", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with less digits, and simply doesn't fill the larger places.
     * @param cs a char array containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 8 or 9 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromHex(final char[] cs, final int start, int end)
    {
        int len, h, lim = 8;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length) - start <= 0 || end > len)
            return 0;
        char c = cs[start];
        if(c == '-')
        {
            len = -1;
            h = 0;
            lim = 9;
        }
        else if(c == '+')
        {
            len = 1;
            h = 0;
            lim = 9;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs[i]) > 102 || (h = hexCodes[c]) < 0)
                return data * len;
            data <<= 4;
            data |= h;
        }
        return data * len;
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * long they represent, reading at most 19 characters (20 if there is a sign) and returning the result if valid, or
     * 0 if nothing could be read. The leading sign can be '+' or '-' if present. Unlike
     * {@link #intFromDec(CharSequence)}, this can't effectively be used to read unsigned longs as decimal literals,
     * since anything larger than the highest signed long would be larger than the normal limit for longs as text (it
     * would be 20 characters without a sign, where we limit it to 19 without a sign to match normal behavior).
     * <br>
     * Should be fairly close to the JDK's Long.parseLong method, but this also supports CharSequence data instead of
     * just String data, and ignores chars after the number. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before the end of cs is reached. If the parse is stopped early, this behaves as you would expect for
     * a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs) {
        return longFromDec(cs,0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * long they represent between the given positions {@code start} and {@code end}, reading at most 19 characters (20
     * if there is a sign) or until end is reached and returning the result if valid, or 0 if nothing could be read. The
     * leading sign can be '+' or '-' if present. Unlike {@link #intFromDec(CharSequence, int, int)}, this can't
     * effectively be used to read unsigned longs as decimal literals, since anything larger than the highest signed
     * long would be larger than the normal limit for longs as text (it would be 20 characters without a sign, where we
     * limit it to 19 without a sign to match normal behavior).
     * <br>
     * Should be fairly close to the JDK's Long.parseLong method, but this also supports CharSequence data instead of
     * just String data, and allows specifying a start and end. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before end is reached. If the parse is stopped early, this behaves as you would expect for a number
     * with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 19 or 20 characters if end is too large, depending on sign)
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 19;
        long sign = 1L;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0L;
        char c = cs.charAt(start);
        if(c == '-')
        {
            sign = -1L;
            lim = 20;
            h = 0;
        }
        else if(c == '+')
        {
            lim = 20;
            h = 0;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0L;
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
                return data * sign;
            data = data * 10 + h;
        }
        return data * sign;
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * int they represent, reading at most 10 characters (11 if there is a sign) and returning the result if valid, or 0
     * if nothing could be read. The leading sign can be '+' or '-' if present. This can technically be used to handle
     * unsigned integers in decimal format, but it isn't the intended purpose. If you do use it for handling unsigned
     * ints, 2147483647 is normally the highest positive int and -2147483648 the lowest negative one, but if you give
     * this a number between 2147483647 and {@code 2147483647 + 2147483648}, it will interpret it as a negative number
     * that fits in bounds using the normal rules for converting between signed and unsigned numbers.
     * <br>
     * Should be fairly close to the JDK's Integer.parseInt method, but this also supports CharSequence data instead of
     * just String data, and ignores chars after the number. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before the end of cs is reached. If the parse is stopped early, this behaves as you would expect for
     * a number with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs) {
        return intFromDec(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only decimal digits (0-9) with an optional sign at the start and returns the
     * int they represent, reading at most 10 characters (11 if there is a sign) and returning the result if valid, or 0
     * if nothing could be read. The leading sign can be '+' or '-' if present. This can technically be used to handle
     * unsigned integers in decimal format, but it isn't the intended purpose. If you do use it for handling unsigned
     * ints, 2147483647 is normally the highest positive int and -2147483648 the lowest negative one, but if you give
     * this a number between 2147483647 and {@code 2147483647 + 2147483648}, it will interpret it as a negative number
     * that fits in bounds using the normal rules for converting between signed and unsigned numbers.
     * <br>
     * Should be fairly close to the JDK's Integer.parseInt method, but this also supports CharSequence data instead of
     * just String data, and allows specifying a start and end. This doesn't throw on invalid input, either, instead
     * returning 0 if the first char is not a decimal digit, or stopping the parse process early if a non-decimal-digit
     * char is read before end is reached. If the parse is stopped early, this behaves as you would expect for a number
     * with less digits, and simply doesn't fill the larger places.
     * @param cs a CharSequence, such as a String, containing only digits 0-9 with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end the (exclusive) last character position in cs to read (this stops after 10 or 11 characters if end is too large, depending on sign)
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs, final int start, int end)
    {
        int len, h, lim = 10;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c == '-')
        {
            len = -1;
            lim = 11;
            h = 0;
        }
        else if(c == '+')
        {
            len = 1;
            lim = 11;
            h = 0;
        }
        else if(c > 102 || (h = hexCodes[c]) < 0 || h > 9)
            return 0;
        else
        {
            len = 1;
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            if((c = cs.charAt(i)) > 102 || (h = hexCodes[c]) < 0 || h > 9)
                return data * len;
            data = data * 10 + h;
        }
        return data * len;
    }
    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs)
    {
        return longFromBin(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the long they represent,
     * reading at most 64 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 64 chars long.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 64 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromBin(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c < '0' || c > '1')
            return 0;
        long data = hexCodes[c];
        for (int i = start+1; i < end && i < start+64; i++) {
            if((c = cs.charAt(i)) < '0' || c > '1')
                return 0;
            data <<= 1;
            data |= c - '0';
        }
        return data;
    }
    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the int they represent,
     * reading at most 32 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 32 chars long.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is a bizarre omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @return the int that cs represents
     */
    public static int intFromBin(CharSequence cs)
    {
        return intFromBin(cs, 0, cs.length());
    }

    /**
     * Reads in a CharSequence containing only binary digits (only 0 and 1) and returns the int they represent,
     * reading at most 32 characters and returning the result if valid or 0 otherwise. The first digit is considered
     * the sign bit iff cs is 32 chars long.
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is a bizarre omission from earlier
     * JDKs. This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only binary digits (nothing at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 32 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromBin(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c < '0' || c > '1')
            return 0;
        int data = hexCodes[c];
        for (int i = start+1; i < end && i < start+32; i++) {
            if((c = cs.charAt(i)) < '0' || c > '1')
                return 0;
            data <<= 1;
            data |= c - '0';
        }
        return data;
    }

    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 11 chars.
     *
     * @param number the long to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 11
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(long number, int offset, char[] buf) {
        if (buf != null && buf.length >= 11 - offset) {
            buf[offset] = keyBase64[(int) (number >>> 60)];
            buf[offset + 1] = keyBase64[(int) (0x3f & number >>> 54)];
            buf[offset + 2] = keyBase64[(int) (0x3f & number >>> 48)];
            buf[offset + 3] = keyBase64[(int) (0x3f & number >>> 42)];
            buf[offset + 4] = keyBase64[(int) (0x3f & number >>> 36)];
            buf[offset + 5] = keyBase64[(int) (0x3f & number >>> 30)];
            buf[offset + 6] = keyBase64[(int) (0x3f & number >>> 24)];
            buf[offset + 7] = keyBase64[(int) (0x3f & number >>> 18)];
            buf[offset + 8] = keyBase64[(int) (0x3f & number >>> 12)];
            buf[offset + 9] = keyBase64[(int) (0x3f & number >>> 6)];
            buf[offset + 10] = keyBase64[(int) (0x3f & number)];
        }
        return buf;
    }


    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 11 chars.
     *
     * @param number the double to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 11
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(double number, int offset, char[] buf) {
        return b64Encode(NumberTools.doubleToLongBits(number), offset, buf);
    }

    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 6 chars.
     *
     * @param number the int to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 6
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(int number, int offset, char[] buf) {
        if (buf != null && buf.length >= 6 - offset) {
            buf[offset] = keyBase64[number >>> 30];
            buf[offset + 1] = keyBase64[0x3f & number >>> 24];
            buf[offset + 2] = keyBase64[0x3f & number >>> 18];
            buf[offset + 3] = keyBase64[0x3f & number >>> 12];
            buf[offset + 4] = keyBase64[0x3f & number >>> 6];
            buf[offset + 5] = keyBase64[0x3f & number];
        }
        return buf;
    }

    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 6 chars.
     *
     * @param number the float to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 6
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(float number, int offset, char[] buf) {
        return b64Encode(NumberTools.floatToIntBits(number), offset, buf);
    }

    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 3 chars.
     *
     * @param number the int to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 3
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(short number, int offset, char[] buf) {
        if (buf != null && buf.length >= 3 - offset) {
            buf[offset] = keyBase64[number >>> 12];
            buf[offset + 1] = keyBase64[0x3f & number >>> 6];
            buf[offset + 2] = keyBase64[0x3f & number];
        }
        return buf;
    }

    /**
     * Base-64 encodes glyph and stores that string representation in buf starting at offset; uses 3 chars.
     *
     * @param glyph  the char to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 3
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(char glyph, int offset, char[] buf) {
        if (buf != null && buf.length >= 4 - offset) {
            buf[offset] = keyBase64[glyph >>> 12];
            buf[offset + 1] = keyBase64[0x3f & glyph >>> 6];
            buf[offset + 2] = keyBase64[0x3f & glyph];
        }
        return buf;
    }

    /**
     * Base-64 encodes number and stores that string representation in buf starting at offset; uses 2 chars.
     *
     * @param number the byte to encode
     * @param offset the first position to set in buf
     * @param buf    a char array that should be non-null and have length of at least offset + 2
     * @return buf, after modifying it in-place
     */
    public static char[] b64Encode(byte number, int offset, char[] buf) {
        if (buf != null && buf.length >= 2 - offset) {
            buf[offset] = keyBase64[number >>> 6];
            buf[offset + 1] = keyBase64[0x3f & number];
        }
        return buf;

    }

    /**
     * Decodes 11 characters from data starting from offset to get a long encoded as base-64.
     * @param data a char array that should be have length of at least offset + 11
     * @param offset where in data to start reading from
     * @return the decoded long
     */
    public static long b64DecodeLong(char[] data, int offset) {
        return (data == null || data.length < 11 + offset) ? 0L :
                (((long)data[offset]) << 60)
                        | ((0x3fL & data[offset + 1]) << 54)
                        | ((0x3fL & data[offset + 2]) << 48)
                        | ((0x3fL & data[offset + 3]) << 42)
                        | ((0x3fL & data[offset + 4]) << 36)
                        | ((0x3fL & data[offset + 5]) << 30)
                        | ((0x3fL & data[offset + 6]) << 24)
                        | ((0x3fL & data[offset + 7]) << 18)
                        | ((0x3fL & data[offset + 8]) << 12)
                        | ((0x3fL & data[offset + 9]) << 6)
                        | (0x3fL & data[offset + 10]);
    }

    /**
     * Decodes 11 characters from data starting from offset to get a double encoded as base-64.
     * @param data a char array that should be have length of at least offset + 11
     * @param offset where in data to start reading from
     * @return the decoded double
     */
    public static double b64DecodeDouble(char[] data, int offset) {
        return (data == null || data.length < 11 + offset) ? 0.0 :
                NumberTools.longBitsToDouble((((long) data[offset]) << 60)
                        | ((0x3fL & data[offset + 1]) << 54)
                        | ((0x3fL & data[offset + 2]) << 48)
                        | ((0x3fL & data[offset + 3]) << 42)
                        | ((0x3fL & data[offset + 4]) << 36)
                        | ((0x3fL & data[offset + 5]) << 30)
                        | ((0x3fL & data[offset + 6]) << 24)
                        | ((0x3fL & data[offset + 7]) << 18)
                        | ((0x3fL & data[offset + 8]) << 12)
                        | ((0x3fL & data[offset + 9]) << 6)
                        | (0x3fL & data[offset + 10]));
    }

    /**
     * Decodes 6 characters from data starting from offset to get an int encoded as base-64.
     * @param data a char array that should be have length of at least offset + 6
     * @param offset where in data to start reading from
     * @return the decoded int
     */
    public static int b64DecodeInt(char[] data, int offset) {
        return (data == null || data.length < 6 + offset) ? 0 :
                ((data[offset]) << 30)
                        | ((0x3f & data[offset + 1]) << 24)
                        | ((0x3f & data[offset + 2]) << 18)
                        | ((0x3f & data[offset + 3]) << 12)
                        | ((0x3f & data[offset + 4]) << 6)
                        | (0x3f & data[offset + 5]);
    }

    /**
     * Decodes 6 characters from data starting from offset to get a float encoded as base-64.
     * @param data a char array that should be have length of at least offset + 6
     * @param offset where in data to start reading from
     * @return the decoded float
     */
    public static float b64DecodeFloat(char[] data, int offset) {
        return (data == null || data.length < 6 + offset) ? 0f :
                NumberTools.intBitsToFloat(((data[offset]) << 30)
                        | ((0x3f & data[offset + 1]) << 24)
                        | ((0x3f & data[offset + 2]) << 18)
                        | ((0x3f & data[offset + 3]) << 12)
                        | ((0x3f & data[offset + 4]) << 6)
                        | (0x3f & data[offset + 5]));
    }

    /**
     * Decodes 3 characters from data starting from offset to get a short encoded as base-64.
     * @param data a char array that should be have length of at least offset + 3
     * @param offset where in data to start reading from
     * @return the decoded short
     */
    public static short b64DecodeShort(char[] data, int offset) {
        return (short) ((data == null || data.length < 3 + offset) ? 0 :
                ((data[offset]) << 12)
                        | ((0x3f & data[offset + 1]) << 6)
                        | (0x3f & data[offset + 2]));
    }
    /**
     * Decodes 3 characters from data starting from offset to get a char encoded as base-64.
     * @param data a char array that should be have length of at least offset + 3
     * @param offset where in data to start reading from
     * @return the decoded char
     */
    public static char b64DecodeChar(char[] data, int offset) {
        return (char) ((data == null || data.length < 3 + offset) ? 0 :
                ((data[offset]) << 12)
                        | ((0x3f & data[offset + 1]) << 6)
                        | (0x3f & data[offset + 2]));
    }

    /**
     * Decodes 2 characters from data starting from offset to get a byte encoded as base-64.
     * @param data a char array that should be have length of at least offset + 2
     * @param offset where in data to start reading from
     * @return the decoded byte
     */
    public static byte b64DecodeByte(char[] data, int offset) {
        return (byte) ((data == null || data.length < 2 + offset) ? 0 :
                ((data[offset]) << 6)
                        | (0x3f & data[offset + 1]));
    }

    public static String hexHash(boolean... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(byte... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(short... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(char... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(int... array) {
        return hex(CrossHash.hash64(array));
    }

    public static String hexHash(long... array) {
        return hex(CrossHash.hash64(array));
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padRight(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, ' ', minimumLength);
        return text;
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the right with padChar
     * until it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padRight(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padRightStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padRightStrict(String text, int totalLength) {
        return padRightStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its right side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padRightStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, 0);
        for (int i = len; i < totalLength; i++) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }

    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with spaces until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with spaces to reach the given minimum length
     */
    public static String padLeft(String text, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, ' ', minimumLength);
        return text;
    }
    /**
     * If text is shorter than the given minimumLength, returns a String with text padded on the left with padChar until
     * it reaches that length; otherwise it simply returns text.
     * @param text the text to pad if necessary
     * @param padChar the char to use to pad text, if necessary
     * @param minimumLength the minimum length of String to return
     * @return text, potentially padded with padChar to reach the given minimum length
     */
    public static String padLeft(String text, char padChar, int minimumLength)
    {
        if(text.length() < minimumLength)
            return padLeftStrict(text, padChar, minimumLength);
        return text;
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with spaces until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly extra spaces
     */
    public static String padLeftStrict(String text, int totalLength) {
        return padLeftStrict(text, ' ', totalLength);
    }

    /**
     * Constructs a String with exactly the given totalLength by taking text (or a substring of it) and padding it on
     * its left side with padChar until totalLength is reached. If text is longer than totalLength, this only uses the
     * portion of text needed to fill totalLength, and no more.
     * @param text the String to pad if necessary, or truncate if too long
     * @param padChar the char to use to fill any remaining length
     * @param totalLength the exact length of String to return
     * @return a String with exactly totalLength for its length, made from text and possibly padChar
     */
    public static String padLeftStrict(String text, char padChar, int totalLength) {
        char[] c = new char[totalLength];
        int len = text.length();
        text.getChars(0, Math.min(len, totalLength), c, Math.max(0, totalLength - len));
        for (int i = totalLength - len - 1; i >= 0; i--) {
            c[i] = padChar;
        }
        return String.valueOf(c);
    }

    /**
     * Word-wraps the given String (or other CharSequence, such as a StringBuilder) so it is split into zero or more
     * Strings as lines of text, with the given width as the maximum width for a line. This correctly splits most (all?)
     * text in European languages on spaces (treating all whitespace characters matched by the regex '\\s' as breaking),
     * and also uses the English-language rule (probably used in other languages as well) of splitting on hyphens and
     * other dash characters (Unicode category Pd) in the middle of a word. This means for a phrase like "UN Secretary
     * General Ban-Ki Moon", if the width was 12, then the Strings in the List returned would be
     * <br>
     * <pre>
     * "UN Secretary"
     * "General Ban-"
     * "Ki Moon"
     * </pre>
     * Spaces are not preserved if they were used to split something into two lines, but dashes are.
     * @param longText a probably-large piece of text that needs to be split into multiple lines with a max width
     * @param width the max width to use for any line, removing trailing whitespace at the end of a line
     * @return a List of Strings for the lines after word-wrapping
     */
    public static List<String> wrap(CharSequence longText, int width)
    {
        if(width <= 0)
            return new ArrayList<>(0);
        return wrap(new ArrayList<String>(longText.length() / width + 2), longText, width);
    }
    /**
     * Word-wraps the given String (or other CharSequence, such as a StringBuilder) so it is split into zero or more
     * Strings as lines of text, with the given width as the maximum width for a line; appends the word-wrapped lines to
     * the given List of Strings and does not create a new List. This correctly splits most (all?) text in European
     * languages on spaces (treating all whitespace characters matched by the regex '\\s' as breaking), and also uses
     * the English-language rule (probably used in other languages as well) of splitting on hyphens and other dash
     * characters (Unicode category Pd) in the middle of a word. This means for a phrase like "UN Secretary General
     * Ban-Ki Moon", if the width was 12, then the Strings in the List returned would be
     * <br>
     * <pre>
     * "UN Secretary"
     * "General Ban-"
     * "Ki Moon"
     * </pre>
     * Spaces are not preserved if they were used to split something into two lines, but dashes are.
     * @param receiving the List of String to append the word-wrapped lines to
     * @param longText a probably-large piece of text that needs to be split into multiple lines with a max width
     * @param width the max width to use for any line, removing trailing whitespace at the end of a line
     * @return the given {@code receiving} parameter, after appending the lines from word-wrapping
     */
    public static List<String> wrap(List<String> receiving, CharSequence longText, int width)
    {
        if(width <= 0 || receiving == null)
            return receiving;
        Matcher widthMatcher = Pattern.compile("(?:({=Y}(?!\\s).{1," + width + "})((?<=\\p{Pd})|(\\s+)))|({=Y}\\S{1," + width + "})").matcher(longText + "\n");
        while (widthMatcher.find())
        {
            receiving.add(widthMatcher.group("Y"));
        }
        return receiving;
    }

    /**
     * Constant storing the 16 hexadecimal digits, as char values, in order.
     */
    public static final char[] hexDigits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    /**
     * A constant containing only chars that are reasonably likely to be supported by broad fonts and thus display-able.
     * This assumes the font supports Latin, Greek, and Cyrillic alphabets, with good support for extended Latin (at
     * least for European languages) but not required to be complete enough to support the very large Vietnamese set of
     * extensions to Latin, nor to support any International Phonetic Alphabet (IPA) chars. It also assumes box drawing
     * characters are supported and a handful of common dingbats, such as male and female signs. It does not include
     * the tab, newline, or carriage return characters, since these don't usually make sense on a grid of chars.
     */
    public static final String PERMISSIBLE_CHARS =
            " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmno"+
            "pqrstuvwxyz{|}~¡¢£¤¥¦§¨©ª«¬®¯°±²³´µ¶·¸¹º»¼½¾¿ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàá"+
            "âãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĜĝĞğĠġĢģĤĥĦħĨĩĪīĬĭĮįİı"+
            "ĴĵĶķĹĺĻļĽľĿŀŁłŃńŅņŇňŊŋŌōŎŏŐőŒœŔŕŖŗŘřŚśŜŝŞşŠšŢţŤťŨũŪūŬŭŮůŰűŲųŴŵŶŷŸŹźŻżŽžſƒǺǻǼǽǾǿ"+
            "ȘșȚțȷˆˇˉˋ˘˙˚˛˜˝΄΅Ά·ΈΉΊΌΎΏΐΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΤΥΦΧΨΩΪΫάέήίΰαβγδεζηθικλμνξοπρςστυ"+
            "φχψωϊϋόύώЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏАБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдежзийклмнопрстуфхц"+
            "чшщъыьэюяѐёђѓєѕіїјљњћќѝўџѴѵҐґẀẁẂẃẄẅỲỳ–—‘’‚‛“”„†‡•…‰‹›ⁿ₤€№™Ω℮←↑→↓∆−√≈" +
            "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬■□▲▼○●◦♀♂♠♣♥♦♪";

    public static final String BOX_DRAWING_SINGLE = "─│┌┐└┘├┤┬┴┼";
    public static final String BOX_DRAWING_DOUBLE = "═║╔╗╚╝╠╣╦╩╬";
    public static final String BOX_DRAWING = "─│┌┐└┘├┤┬┴┼═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬";
    public static final String VISUAL_SYMBOLS = "←↑→↓■□▲▼○●◦♀♂♠♣♥♦♪";
    public static final String DIGITS = "0123456789";
    public static final String MARKS = "~`^'¨¯°´¸ˆˇˉˋ˘˙˚˛˜˝΄΅‘’‚‛";
    /**
     * Can be used to match an index with one in {@link #GROUPING_SIGNS_CLOSE} to find the closing char (this way only).
     */
    public static final String GROUPING_SIGNS_OPEN  = "([{<«‘‛“‹";
    /**
     * An index in {@link #GROUPING_SIGNS_OPEN} can be used here to find the closing char for that opening one.
     */
    public static final String GROUPING_SIGNS_CLOSE = ")]}>»’’”›";
    public static final String COMMON_PUNCTUATION = "!\"%&'*+,-./:;<>?•…–—";
    public static final String MODERN_PUNCTUATION = "@\\^_`|~¦©®™´№♀♂♪";
    public static final String UNCOMMON_PUNCTUATION = "§¶¨ªº¯°·¸¡¿·‚„†‡";
    public static final String TECHNICAL_PUNCTUATION = "#%'*+,-./<=>^|¬°µ±¹²³ⁿ¼½¾×÷‰№Ω℮∆−√≈";
    public static final String PUNCTUATION = COMMON_PUNCTUATION + MODERN_PUNCTUATION + UNCOMMON_PUNCTUATION +
            TECHNICAL_PUNCTUATION + GROUPING_SIGNS_OPEN + GROUPING_SIGNS_CLOSE;
    public static final String CURRENCY = "$¢£¤¥₤€";
    public static final String SPACING = " ";
    public static final String ENGLISH_LETTERS_UPPER =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String ENGLISH_LETTERS_LOWER =
            "abcdefghijklmnopqrstuvwxyz";
    public static final String ENGLISH_LETTERS = ENGLISH_LETTERS_UPPER + ENGLISH_LETTERS_LOWER;

    public static final String LATIN_EXTENDED_LETTERS_UPPER =
            "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞĀĂĄĆĈĊČĎĐĒĔĖĘĚĜĞĠĢĤĦĨĪĬĮİĴĶĹĻĽĿŁŃŅŇŊŌŎŐŒŔŖŘŚŜŞŠŢŤŨŪŬŮŰŲŴŶŸŹŻŽǺǼǾȘȚẀẂẄỲßSFJ";
    public static final String LATIN_EXTENDED_LETTERS_LOWER =
            "àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþāăąćĉċčďđēĕėęěĝğġģĥħĩīĭįıĵķĺļľŀłńņňŋōŏőœŕŗřśŝşšţťũūŭůűųŵŷÿźżžǻǽǿșțẁẃẅỳßſƒȷ";
    public static final String LATIN_EXTENDED_LETTERS = LATIN_EXTENDED_LETTERS_UPPER + LATIN_EXTENDED_LETTERS_LOWER;

    public static final String LATIN_LETTERS_UPPER = ENGLISH_LETTERS_UPPER + LATIN_EXTENDED_LETTERS_UPPER;
    public static final String LATIN_LETTERS_LOWER = ENGLISH_LETTERS_LOWER + LATIN_EXTENDED_LETTERS_LOWER;
    public static final String LATIN_LETTERS = LATIN_LETTERS_UPPER + LATIN_LETTERS_LOWER;

    /**
     * Includes the letter Sigma, 'Σ', twice because it has two lower-case forms in {@link #GREEK_LETTERS_LOWER}. This
     * lets you use one index for both lower and upper case, like with Latin and Cyrillic.
     */
    public static final String GREEK_LETTERS_UPPER =
            "ΑΒΓΔΕΖΗΘΙΚΛΜΝΞΟΠΡΣΣΤΥΦΧΨΩΆΈΉΊΌΎΏΪΫΪΫ";
    /**
     * Includes both lower-case forms for Sigma, 'ς' and 'σ', but this matches the two upper-case Sigma in
     * {@link #GREEK_LETTERS_UPPER}. This lets you use one index for both lower and upper case, like with Latin and
     * Cyrillic.
     */
    public static final String GREEK_LETTERS_LOWER =
            "αβγδεζηθικλμνξοπρςστυφχψωάέήίόύώϊϋΐΰ";

    public static final String GREEK_LETTERS = GREEK_LETTERS_UPPER + GREEK_LETTERS_LOWER;

    public static final String CYRILLIC_LETTERS_UPPER =
            "АБВГДЕЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯЀЁЂЃЄЅІЇЈЉЊЋЌЍЎЏѴҐ";
    public static final String CYRILLIC_LETTERS_LOWER =
            "абвгдежзийклмнопрстуфхцчшщъыьэюяѐёђѓєѕіїјљњћќѝўџѵґ";
    public static final String CYRILLIC_LETTERS = CYRILLIC_LETTERS_UPPER + CYRILLIC_LETTERS_LOWER;

    public static final String LETTERS_UPPER = LATIN_LETTERS_UPPER + GREEK_LETTERS_UPPER + CYRILLIC_LETTERS_UPPER;
    public static final String LETTERS_LOWER = LATIN_LETTERS_LOWER + GREEK_LETTERS_LOWER + CYRILLIC_LETTERS_LOWER;
    public static final String LETTERS = LETTERS_UPPER + LETTERS_LOWER;
    public static final String LETTERS_AND_NUMBERS = LETTERS + DIGITS;

    public static String replace(CharSequence text, String before, String after) {
        if(text instanceof String)
        {
            return ((String)text).replace(before, after);
        }
        String t = text.toString();
        return t.replace(before, after);
    }

}
