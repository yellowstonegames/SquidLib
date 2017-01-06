package squidpony;

import squidpony.squidmath.CrossHash;

import java.util.Collection;
import java.util.Iterator;

/**
 * Various utility functions for dealing with Strings, CharSequences, and char[]s; mostly converting numbers.
 * Created by Tommy Ettinger on 3/21/2016.
 */
public class StringKit {

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
            sb.append(delimiter).append(elements[i]).append('L');
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
    public static int count(String source, String search)
    {
        if(source == null || search == null || source.isEmpty() || search.isEmpty())
            return 0;
        int amount = 0, idx = -1;
        while ((idx = source.indexOf(search, idx+1)) >= 0)
            ++amount;
        return amount;
    }

    /**
     * Like {@link String#substring(int, int)} but returns "" instead of throwing any sort of Exception.
     * @param source the String to get a substring from
     * @param beginIndex the first index, inclusive; will be treated as 0 if negative
     * @param endIndex the index after the last character (i.e. length, so exclusive); if negative this returns ""
     * @return the substring of source between beginIndex and endIndex, or "" if any parameters are null/invalid
     */
    public static String safeSubstring(String source, int beginIndex, int endIndex)
    {
        if(endIndex < 0 || source == null || source.isEmpty()) return "";
        if(beginIndex < 0) beginIndex = 0;
        if(endIndex > source.length()) endIndex = source.length();
        if(beginIndex > endIndex) return "";
        return source.substring(beginIndex, endIndex);
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
        String[] splat = new String[++amount];
        int dl = delimiter.length(), idx = -dl;
        for (int i = 0; i < amount; i++) {
            splat[i] = safeSubstring(source, idx+dl, idx = source.indexOf(delimiter, idx+dl));
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
        String h = Integer.toHexString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(char number) {
        String h = Integer.toHexString(number & 0xffff);
        return mask16.substring(0, 16 - h.length()) + h;
    }

    public static String bin(byte number) {
        String h = Integer.toHexString(number & 0xff);
        return mask8.substring(0, 8 - h.length()) + h;
    }
    private static final int[] hexCodes = new int[]
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0,
             0, 10, 11, 12, 13, 14, 15, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
             0, 10, 11, 12, 13, 14, 15};

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) and returns the long they represent,
     * reading at most 16 characters and returning the result if valid or 0 otherwise. This does not represent negative
     * numbers except as they are printed by such methods as String.format given a %x in the formatting string, or this
     * class' {@link #hex(long)} method; that is, if the first char of a 16-char CharSequence is a hex digit 8 or
     * higher, then the whole number represents a negative number, using two's complement and so on. This means
     * "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only hex digits (no 0x at the start)
     * @return the long that cs represents
     */
    public static long longFromHex(CharSequence cs) {
        return longFromHex(cs, 0, cs.length());
    }
    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) and returns the long they represent,
     * reading at most 16 characters and returning the result if valid or 0 otherwise. This does not represent negative
     * numbers except as they are printed by such methods as String.format given a %x in the formatting string, or this
     * class' {@link #hex(long)} method; that is, if the first char of a 16-char CharSequence is a hex digit 8 or
     * higher, then the whole number represents a negative number, using two's complement and so on. This means
     * "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this.
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is a bizarre omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0.
     * @param cs a CharSequence, such as a String, containing only hex digits (no 0x at the start)
     * @param start the first character position in cs to read from
     * @param end the last character position in cs to read from (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(CharSequence cs, final int start, final int end)
    {
        int len;
        if(cs == null || start < 0 || end <=0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
            return 0;
        char c = cs.charAt(start);
        if(c > 102)
            return 0;
        long data = hexCodes[c];
        for (int i = start+1; i < end && i < 16; i++) {
            if((c = cs.charAt(i)) > 102)
                return 0;
            data <<= 4;
            data |= hexCodes[c];
        }
        return data;
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
        for (int i = start+1; i < end && i < 64; i++) {
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
        return b64Encode(Double.doubleToLongBits(number), offset, buf);
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
        return b64Encode(Float.floatToIntBits(number), offset, buf);
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
                Double.longBitsToDouble((((long) data[offset]) << 60)
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
                Float.intBitsToFloat(((data[offset]) << 30)
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
}
